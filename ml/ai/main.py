"""
malaria_detection_fullfarsh.py

Полный GPU-пайплайн для ВКР:
- Один проход или 5-fold CV (--folds N или 0)
- Train метрики & Test метрики отдельно
- Все популярные метрики: Accuracy, Balanced Acc, Precision, Recall, Specificity, F1, ROC-AUC, PR-AUC, MCC, Cohen Kappa, Log-Loss
- Графики: ROC, Precision-Recall, Calibration, матрицы ошибок, learning curves, PCA, t-SNE
- Grad-CAM визуализации
- Экспорт TorchScript (обязательно) и ONNX (если установлен пакет onnx)
- Windows-совместимость (num_workers = 0 по умолчанию)

Запуск примера:
    python malaria_detection_fullfarsh.py --epochs 5 --folds 0 --workers 0

Все результаты сохраняются в папку reports/.
"""

from __future__ import annotations
import argparse, json, os, random, time
from pathlib import Path
from typing import List

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
import torch
import torch.nn as nn
import torch.optim as optim
from sklearn.calibration import calibration_curve
from sklearn.decomposition import PCA
from sklearn.manifold import TSNE
from sklearn.metrics import (
    accuracy_score, balanced_accuracy_score, cohen_kappa_score,
    confusion_matrix, f1_score, log_loss, matthews_corrcoef,
    precision_recall_curve, precision_score, recall_score,
    roc_auc_score, roc_curve, average_precision_score
)
from sklearn.model_selection import StratifiedKFold, train_test_split
from torch.utils.data import DataLoader, Subset
from torchvision import datasets, models, transforms
from tqdm.auto import tqdm

parser = argparse.ArgumentParser("Malaria detection full-farsh GPU pipeline")
parser.add_argument('--epochs',  type=int, default=7,   help='макс. эпох')
parser.add_argument('--folds',   type=int, default=0,   help='кол-во фолдов CV: 0=без CV')
parser.add_argument('--workers', type=int, default=0,   help='num_workers DataLoader')
args = parser.parse_args()

if torch.cuda.is_available():
    device = torch.device('cuda')
    torch.backends.cudnn.benchmark = True
elif torch.backends.mps.is_available():
    device = torch.device('mps')
else:
    device = torch.device('cpu')
    print("Используется CPU")
print(f"> Используем устройство: {device}")
torch.backends.cudnn.benchmark = True

SEED = 42
random.seed(SEED); np.random.seed(SEED); torch.manual_seed(SEED)
DATA_DIR = Path("../../ml/training/data/train")
REPORTS  = REPORTS = Path("../training"); REPORTS.mkdir(exist_ok=True)
IMG_SIZE, BATCH, LR = 224, 8, 3e-4
PATIENCE, EPS       = 2,   1e-4

train_tfms = transforms.Compose([
    transforms.RandomResizedCrop(IMG_SIZE, scale=(0.8,1.0)),
    transforms.RandomHorizontalFlip(), transforms.RandomVerticalFlip(),
    transforms.RandomRotation(20), transforms.ColorJitter(0.2,0.2,0.2,0.05),
    transforms.ToTensor(), transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
])
val_tfms = transforms.Compose([
    transforms.Resize((IMG_SIZE,IMG_SIZE)), transforms.ToTensor(),
    transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
])

if not DATA_DIR.exists():
    raise FileNotFoundError(f"Dataset folder not found: {DATA_DIR}")
full_ds = datasets.ImageFolder(root=DATA_DIR)
labels  = np.array(full_ds.targets)
# === ДИАГНОСТИКА ДАННЫХ ===
print("=== ДИАГНОСТИКА ДАННЫХ ===")
print("Размер датасета:", len(full_ds))
print("Классы:", full_ds.classes)
print("Распределение по классам:", np.bincount(labels))
print("Соотношение классов:", np.bincount(labels) / len(labels))

# Проверь первые несколько изображений
for i in range(3):
    img, label = full_ds[i]
    print(f"Изображение {i}: класс {full_ds.classes[label]}, размер {img.size}")
# === КОНЕЦ ДИАГНОСТИКИ ===

num_workers = args.workers
pin_memory   = True

def make_loader(idxs: List[int], train: bool) -> DataLoader:
    subset = Subset(full_ds, idxs)
    subset.dataset.transform = train_tfms if train else val_tfms
    return DataLoader(subset,
                      batch_size=BATCH,
                      shuffle=train,
                      num_workers=num_workers,
                      pin_memory=pin_memory)

def build_model() -> nn.Module:
    model = models.efficientnet_b0(weights=models.EfficientNet_B0_Weights.DEFAULT)
   # for p in model.features.parameters(): p.requires_grad = False
    model.classifier = nn.Sequential(
        nn.Dropout(0.3),
        nn.Linear(model.classifier[1].in_features, 2)
    )
    return model.to(device)

def run_epoch(loader, net, criterion, optimizer=None, desc=""):
    training = optimizer is not None
    if training: net.train()
    else:       net.eval()
    total_loss, gts, preds = 0.0, [], []
    for x, y in tqdm(loader, desc=desc, total=len(loader), leave=False):
        x, y = x.to(device), y.to(device)
        with torch.set_grad_enabled(training):
            out  = net(x)
            loss = criterion(out, y)
            if training:
                optimizer.zero_grad(); loss.backward(); optimizer.step()
        total_loss += loss.item() * x.size(0)
        gts.extend(y.cpu().numpy())
        preds.extend(out.argmax(1).cpu().numpy())
    return total_loss / len(loader.dataset), accuracy_score(gts, preds)

def compute_metrics(y_true, y_prob, y_pred) -> dict:
    return {
        'accuracy'         : accuracy_score(y_true, y_pred),
        'balanced_accuracy': balanced_accuracy_score(y_true, y_pred),
        'precision'        : precision_score(y_true, y_pred),
        'recall'           : recall_score(y_true, y_pred),
        'specificity'      : recall_score(y_true, y_pred, pos_label=0),
        'f1'               : f1_score(y_true, y_pred),
        'roc_auc'          : roc_auc_score(y_true, y_prob),
        'pr_auc'           : average_precision_score(y_true, y_prob),
        'mcc'              : matthews_corrcoef(y_true, y_pred),
        'cohen_kappa'      : cohen_kappa_score(y_true, y_pred),
        'log_loss'         : log_loss(y_true, y_prob)
    }

def pretty_print(title: str, m: dict):
    print(f"\n{title}\n{'-'*len(title)}")
    df = pd.DataFrame([m]).T.rename(columns={0:'value'})
    print(df.to_string(header=False))

if args.folds > 0:
    cv_results = []
    skf = StratifiedKFold(n_splits=args.folds, shuffle=True, random_state=SEED)
    for fold, (tr_idx, vl_idx) in enumerate(skf.split(np.arange(len(labels)), labels), start=1):
        print(f"\nCV Fold {fold}/{args.folds}")
        tr_loader = make_loader(tr_idx, True)
        vl_loader = make_loader(vl_idx, False)
        net = build_model()
        criterion = nn.CrossEntropyLoss()
        optimizer = optim.AdamW(net.parameters(), lr=LR)
        best_acc, wait = 0.0, 0
        for ep in range(1, args.epochs+1):
            run_epoch(tr_loader, net, criterion, optimizer, desc=f"Fold{fold} Ep{ep}[train]")
            _, val_acc = run_epoch(vl_loader, net, criterion, None, desc=f"Fold{fold} Ep{ep}[val]")
            if val_acc > best_acc + EPS:
                best_acc, wait = val_acc, 0
                torch.save(net.state_dict(), REPORTS/f'cv_fold{fold}.pt')
            else:
                wait += 1
            if wait >= PATIENCE:
                break
        # load best and eval
        net.load_state_dict(torch.load(REPORTS/f'cv_fold{fold}.pt'))
        net.eval()
        probs, preds, truths = [], [], []
        with torch.no_grad():
            for x, y in vl_loader:
                out = net(x.to(device))
                probs_tensor = torch.softmax(out, 1)
                predicted_class = out.argmax(1)
                p = probs_tensor[torch.arange(len(probs_tensor)), predicted_class]
                probs.extend(p.cpu().numpy());
                preds.extend(predicted_class.cpu().numpy());
                truths.extend(y.numpy())
        cv_results.append(compute_metrics(np.array(truths), np.array(probs), np.array(preds)))
    pd.DataFrame(cv_results).to_csv(REPORTS/'cv_metrics.csv', index=False)
    print("CV metrics saved.")

if len(labels) < 20:
    test_size = max(2, len(labels) // 3)  # минимум 2 изображения в test
    train_idx, test_idx = train_test_split(
        np.arange(len(labels)),
        test_size=test_size,
        stratify=labels,
        random_state=SEED
    )
else:
    train_idx, test_idx = train_test_split(
        np.arange(len(labels)),
        test_size=0.15,
        stratify=labels,
        random_state=SEED
    )
train_loader = make_loader(train_idx, True)
test_loader  = make_loader(test_idx, False)

model = build_model()
criterion = nn.CrossEntropyLoss()
optimizer = optim.AdamW(model.parameters(), lr=LR)
train_hist, val_hist = [], []
best_acc, wait = 0.0, 0
for ep in range(1, args.epochs+1):
    tloss, _ = run_epoch(train_loader, model, criterion, optimizer, desc=f"Epoch {ep}/{args.epochs}[train]")
    train_hist.append(tloss)
    _, vacc  = run_epoch(test_loader,  model, criterion, None,      desc=f"Epoch {ep}/{args.epochs}[val]")
    val_hist.append(vacc)
    if vacc > best_acc + EPS:
        best_acc, wait = vacc, 0
        torch.save(model.state_dict(), REPORTS/'best.pt')
    else:
        wait += 1
    if wait >= PATIENCE:
        print("Early stopping")
        break

model.eval()
probs_tr, preds_tr, truths_tr = [], [], []
with torch.no_grad():
    for x, y in train_loader:
        out = model(x.to(device))
        probs_tensor = torch.softmax(out, 1)
        predicted_class = out.argmax(1)
        p = probs_tensor[torch.arange(len(probs_tensor)), predicted_class]
        probs_tr.extend(p.cpu().numpy()); preds_tr.extend(out.argmax(1).cpu().numpy()); truths_tr.extend(y.numpy())
train_metrics = compute_metrics(np.array(truths_tr), np.array(probs_tr), np.array(preds_tr))
pd.DataFrame([train_metrics]).to_csv(REPORTS/'train_metrics.csv', index=False)

model.load_state_dict(torch.load(REPORTS/'best.pt'))
model.eval()
probs_te, preds_te, truths_te = [], [], []
with torch.no_grad():
    for x, y in test_loader:
        out = model(x.to(device))
        probs_tensor = torch.softmax(out, 1)
        predicted_class = out.argmax(1)
        p = probs_tensor[torch.arange(len(probs_tensor)), predicted_class]
        probs_te.extend(p.cpu().numpy()); preds_te.extend(out.argmax(1).cpu().numpy()); truths_te.extend(y.numpy())
test_metrics = compute_metrics(np.array(truths_te), np.array(probs_te), np.array(preds_te))
pd.DataFrame([test_metrics]).to_csv(REPORTS/'test_metrics.csv', index=False)

pretty_print = lambda title, m: (print(f"\n{title}\n{'-'*len(title)}"), print(pd.DataFrame([m]).T.to_string(header=False)) )
pretty_print("Train metrics", train_metrics)
pretty_print("Test  metrics", test_metrics)

fpr,tpr,_ = roc_curve(truths_te, probs_te)
plt.figure(); plt.plot(fpr,tpr,label=f"AUC={test_metrics['roc_auc']:.3f}"); plt.plot([0,1],[0,1],'--'); plt.savefig(REPORTS/'roc.png'); plt.close()

prec,rec,_ = precision_recall_curve(truths_te, probs_te)
plt.figure(); plt.plot(rec,prec,label=f"AUPRC={test_metrics['pr_auc']:.3f}"); plt.savefig(REPORTS/'pr.png'); plt.close()

prob_true,prob_pred = calibration_curve(truths_te, probs_te, n_bins=10)
plt.figure(); plt.plot(prob_pred,prob_true,'o-'); plt.plot([0,1],[0,1],'--'); plt.savefig(REPORTS/'calibration.png'); plt.close()

cm = confusion_matrix(truths_te, preds_te)
sns.heatmap(cm,annot=True,fmt='d',cmap='Blues'); plt.savefig(REPORTS/'confusion.png'); plt.close()
cm_n = cm/cm.sum(axis=1,keepdims=True)
sns.heatmap(cm_n,annot=True,fmt='.2f',cmap='Blues'); plt.savefig(REPORTS/'confusion_norm.png'); plt.close()

plt.figure(); plt.plot(train_hist,label='train_loss'); plt.twinx(); plt.plot(val_hist,label='val_acc',color='orange'); plt.savefig(REPORTS/'learning_curves.png'); plt.close()

feat_list = []
truths_te_np = np.array(truths_te)

with torch.no_grad():
    for x, _ in test_loader:
        f = model.features(x.to(device)).mean(dim=[2, 3]).cpu().numpy()
        feat_list.append(f)

if len(feat_list) > 0:
    feats = np.vstack(feat_list)

    # PCA - работает даже с 2 образцами
    if len(feats) >= 2:
        n_components = min(2, len(feats) - 1, feats.shape[1])
        pca_res = PCA(n_components=n_components).fit_transform(feats)
        plt.figure(figsize=(8, 6))

        if n_components == 2:
            # Если есть 2 компоненты - обычный scatter plot
            sns.scatterplot(x=pca_res[:, 0], y=pca_res[:, 1], hue=truths_te_np, palette='Set1')
            plt.xlabel('Principal Component 1')
            plt.ylabel('Principal Component 2')
        else:
            # Если только 1 компонента - используем bar plot или одномерный scatter
            plt.scatter(pca_res[:, 0], np.zeros_like(pca_res[:, 0]), c=truths_te_np, cmap='Set1')
            plt.xlabel('Principal Component 1')
            plt.yticks([])  # убираем ось Y

        plt.title('PCA Visualization')
        plt.savefig(REPORTS / 'pca.png')
        plt.close()
        print("PCA визуализация сохранена")

    # t-SNE - требует больше образцов
    if len(feats) >= 5:  # минимум 5 образцов для t-SNE
        perplexity_val = min(5, len(feats) - 1)  # адаптивный perplexity
        tsne_res = TSNE(n_components=2, init='pca', random_state=SEED,
                        perplexity=perplexity_val).fit_transform(feats)
        plt.figure(figsize=(8, 6))
        sns.scatterplot(x=tsne_res[:, 0], y=tsne_res[:, 1], hue=truths_te_np, palette='Set1')
        plt.title('t-SNE Visualization')
        plt.savefig(REPORTS / 'tsne.png')
        plt.close()
        print("t-SNE визуализация сохранена")
    else:
        print(f"t-SNE пропущен: требуется минимум 5 образцов, доступно {len(feats)}")
else:
    print("Недостаточно данных для визуализаций")
try:
    from pytorch_grad_cam import GradCAM
    from pytorch_grad_cam.utils.image import show_cam_on_image
    cam = GradCAM(model=model,target_layers=[model.features[-1]],use_cuda=True)
    demo_loader = make_loader(test_idx[:4],False)
    imgs,_ = next(iter(demo_loader)); masks = cam(input_tensor=imgs.to(device))
    for i,m in enumerate(masks):
        img = imgs[i].permute(1,2,0).numpy()
        img = np.clip(img*[0.229,0.224,0.225]+[0.485,0.456,0.406],0,1)
        vis = show_cam_on_image(img,m,use_rgb=True,image_weight=0.5)
        plt.imsave(REPORTS/f'gradcam_{i}.png',vis)
except ImportError:
    print('Grad-CAM недоступен')


params = sum(p.numel() for p in model.parameters())/1e6
size_mb = (REPORTS/'best.pt').stat().st_size/1e6
start = time.time(); model(torch.randn(1,3,IMG_SIZE,IMG_SIZE).to(device)); inf_ms=(time.time()-start)*1000
print(f"Параметров: {params:.2f}M | Вес: {size_mb:.1f}MB | Инференс: {inf_ms:.1f}ms")

torch.jit.script(model.cpu()).save(REPORTS/'malaria_scripted.pt')

try:
    import onnx
    torch.onnx.export(model.cpu(),torch.randn(1,3,IMG_SIZE,IMG_SIZE),REPORTS/'malaria.onnx',opset_version=17)
    print("ONNX-модель сохранена")
except ModuleNotFoundError:
    print("onnx не установлен, экспорт пропущен")

if __name__=='__main__': import multiprocessing as mp; mp.freeze_support()
