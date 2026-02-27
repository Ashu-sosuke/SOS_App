import os

class Config:
    # Model Paths (can be local paths or HuggingFace IDs)
    WHISPER_MODEL_SIZE = os.getenv("WHISPER_MODEL_SIZE", "tiny") # Use 'tiny' or 'base' for speed, 'small'/'medium' for accuracy
    THREAT_MODEL_ID = os.getenv("THREAT_MODEL_ID", "distilbert-base-uncased-finetuned-sst-2-english") # Placeholder, usually would be a fine-tuned model
    
    # Audio Settings
    MAX_AUDIO_DURATION_SEC = 300  # 5 minutes
    ALLOWED_AUDIO_TYPES = ["audio/wav", "audio/mpeg", "audio/mp3", "audio/ogg", "audio/x-wav"]
    
    # Feature Toggles
    ENABLE_EMOTION_DETECTION = os.getenv("ENABLE_EMOTION_DETECTION", "True").lower() == "true"
    
    # Weights for Severity Fusion
    WEIGHT_STRESS = 0.4
    WEIGHT_THREAT = 0.3
    WEIGHT_KEYWORD = 0.2
    WEIGHT_LOCATION = 0.1

config = Config()
