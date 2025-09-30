# ml-api/app.py
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image
import io
import time
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Malaria Detection API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Конфигурация из ии
IMG_SIZE = 224
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
logger.info(f"Using device: {device}")

val_tfms = transforms.Compose([
    transforms.Resize((IMG_SIZE, IMG_SIZE)),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
])


def build_model() -> nn.Module:
    """Функция создания модели из вашего кода"""
    model = models.efficientnet_b0(weights=models.EfficientNet_B0_Weights.DEFAULT)
    for p in model.features.parameters():
        p.requires_grad = False
    model.classifier = nn.Sequential(
        nn.Dropout(0.3),
        nn.Linear(model.classifier[1].in_features, 2)
    )
    return model.to(device)


# Загружаем модель (замените путь на ваш сохраненный файл модели)
try:
    model = build_model()
    # ЗАМЕНИТЕ ЭТУ СТРОКУ НА ПУТЬ К ВАШЕЙ СОХРАНЕННОЙ МОДЕЛИ
    model.load_state_dict(torch.load('reports/best.pt', map_location=device))
    model.eval()
    logger.info("✅ Model loaded successfully")
except Exception as e:
    logger.error(f"❌ Error loading model: {e}")
    model = None


def predict(image_data: bytes) -> dict:
    """Основная функция предсказания на основе вашего кода"""
    if model is None:
        return {"diagnosis": "error", "confidence": 0.0, "error": "Model not loaded"}

    try:
        start_time = time.time()

        # Обработка изображения
        image = Image.open(io.BytesIO(image_data)).convert('RGB')
        image_tensor = val_tfms(image).unsqueeze(0).to(device)

        # Предсказание (из вашего кода)
        with torch.no_grad():
            output = model(image_tensor)
            probability = torch.softmax(output, dim=1)[0][1].item()

        processing_time = time.time() - start_time

        # Определяем диагноз на основе порога 0.5
        diagnosis = "parasitized" if probability > 0.5 else "uninfected"

        return {
            "diagnosis": diagnosis,
            "confidence": probability,
            "processing_time": round(processing_time, 2),
            "model_used": "EfficientNet-B0"
        }

    except Exception as e:
        logger.error(f"Prediction error: {e}")
        return {
            "diagnosis": "error",
            "confidence": 0.0,
            "processing_time": 0.0,
            "error": str(e)
        }


@app.post("/analyze")
async def analyze_image(image: UploadFile = File(...)):
    """API endpoint для анализа изображения"""
    try:
        # Проверяем тип файла
        if not image.content_type.startswith('image/'):
            return {"error": "File is not an image"}

        # Читаем изображение
        image_data = await image.read()

        if len(image_data) == 0:
            return {"error": "Empty image file"}

        # Вызываем функцию предсказания
        result = predict(image_data)

        return result

    except Exception as e:
        logger.error(f"API error: {e}")
        return {"error": f"Processing error: {str(e)}"}


@app.get("/health")
async def health_check():
    """Проверка здоровья сервера"""
    status = "healthy" if model is not None else "model_not_loaded"
    return {
        "status": status,
        "service": "malaria-ml-api",
        "device": str(device),
        "model_loaded": model is not None
    }


@app.get("/model-info")
async def model_info():
    """Информация о модели"""
    if model is None:
        return {"error": "Model not loaded"}

    total_params = sum(p.numel() for p in model.parameters())
    return {
        "model_name": "EfficientNet-B0",
        "total_parameters": total_params,
        "input_size": IMG_SIZE,
        "device": str(device)
    }


if __name__ == "__main__":
    logger.info("🚀 Starting ML API server on http://localhost:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")