from fastapi import FastAPI, HTTPException, BackgroundTasks
from schemas import IncidentInput, IncidentOutput
from utils.audio_loader import AudioLoader, logger as audio_logger
from models.transcription import TranscriptionService, logger as trans_logger
from models.emotion import AudioStressDetector, logger as emotion_logger
from models.threat_classifier import ThreatClassifier, logger as threat_logger
from models.fusion import FusionEngine
import logging
import os
import uvicorn
import time

# Configure Logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("main")

app = FastAPI(title="SOS Intelligence AI Service", version="1.0.0")

# Global models (loaded on startup)
transcription_service = None
audio_stress_detector = None
threat_classifier = None

@app.on_event("startup")
async def startup_event():
    global transcription_service, audio_stress_detector, threat_classifier
    logger.info("Initializing models...")
    try:
        transcription_service = TranscriptionService()
        audio_stress_detector = AudioStressDetector()
        threat_classifier = ThreatClassifier()
        logger.info("All models initialized successfully.")
    except Exception as e:
        logger.critical(f"Model initialization failed: {e}")
        # We might want to exit here if models are critical
        raise e

@app.get("/health")
def health_check():
    """Health check endpoint for Cloud Run."""
    if not (transcription_service and audio_stress_detector and threat_classifier):
        raise HTTPException(status_code=503, detail="Models not fully loaded")
    return {"status": "healthy", "version": "1.0.0"}

@app.post("/process-incident", response_model=IncidentOutput)
async def process_incident(incident: IncidentInput, background_tasks: BackgroundTasks):
    """
    Main processing pipeline for SOS incidents.
    """
    logger.info(f"Received processing request for incident: {incident.incidentId}")
    start_time = time.time()
    temp_audio_path = None
    
    try:
        # 1. Download Audio
        logger.info("Step 1: Downloading audio...")
        temp_audio_path = AudioLoader.download_audio(incident.audioUrl)
        
        # 2. Transcription
        logger.info("Step 2: Transcribing audio...")
        transcription_result = transcription_service.transcribe(temp_audio_path)
        transcript_text = transcription_result["text"]
        
        # 3. Emotion/Stress Analysis
        logger.info("Step 3: Analyzing emotion/stress...")
        emotion_result = audio_stress_detector.analyze(temp_audio_path)
        stress_score = emotion_result["stress_score"]
        
        # 4. Threat Classification
        logger.info("Step 4: Classifying threat...")
        threat_result = threat_classifier.classify(transcript_text)
        
        # 5. Fusion
        logger.info("Step 5: Computing fusion score...")
        # Simple location risk heuristic (placeholder: real system would query a risk map)
        location_risk = 0.0 
        
        # Keyword score is partially handled in threat classifier override, 
        # but we can pass explicit 1.0 if specific keywords were found if we wanted to separate it.
        # For now, we assume threat_classifier handles the text-based logic.
        
        fusion_result = FusionEngine.compute_severity(
            stress_score=stress_score,
            threat_data=threat_result,
            keyword_score=0.0, # handled within threat/stress implicitly for this MVP
            location_risk=location_risk
        )
        
        response = IncidentOutput(
            incidentId=incident.incidentId,
            transcript=transcript_text,
            stressScore=stress_score,
            threatType=threat_result["threat_type"],
            severityScore=fusion_result["final_score"],
            finalSeverity=fusion_result["severity_level"],
            confidence=threat_result["confidence"],
            recommendedAction=fusion_result["recommended_action"],
            details={
                "processing_time_sec": round(time.time() - start_time, 2),
                "emotion_details": emotion_result.get("details"),
                "fusion_breakdown": fusion_result.get("breakdown")
            }
        )
        
        logger.info(f"Processing complete for {incident.incidentId}. Severity: {response.finalSeverity}")
        return response

    except Exception as e:
        logger.error(f"Error processing incident {incident.incidentId}: {e}")
        raise HTTPException(status_code=500, detail=str(e))
        
    finally:
        # Clean up temp file in background
        if temp_audio_path:
            background_tasks.add_task(AudioLoader.cleanup_file, temp_audio_path)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))
