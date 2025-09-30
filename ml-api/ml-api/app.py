# ml-api/app.py
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from PIL import Image
import io

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


def predict(image_data: bytes) -> dict:
    """Простая заглушка для теста"""
    try:
        # Просто открываем изображение чтобы проверить работу
        image = Image.open(io.BytesIO(image_data))
        width, height = image.size

        return {
            "diagnosis": "parasitized",
            "confidence": 0.95,
            "time": 1.5,
            "image_size": f"{width}x{height}"
        }
    except Exception as e:
        return {
            "diagnosis": "error",
            "confidence": 0.0,
            "time": 0.0,
            "error": str(e)
        }


@app.post("/analyze")
async def analyze_image(image: UploadFile = File(...)):
    try:
        image_data = await image.read()
        result = predict(image_data)
        return result
    except Exception as e:
        return {"error": str(e)}


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "malaria-ml-api"}


if __name__ == "__main__":
    print("🚀 Starting ML API server on http://localhost:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)