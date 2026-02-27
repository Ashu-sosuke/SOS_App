import logging
from faster_whisper import WhisperModel
from config import config

logger = logging.getLogger(__name__)

class TranscriptionService:
    def __init__(self):
        # Load model on initialization
        # Run on CPU for cost efficiency on Cloud Run unless GPU is explicitly provisioned
        # 'int8' quantization is faster on CPU
        logger.info(f"Loading Whisper model: {config.WHISPER_MODEL_SIZE}...")
        try:
            self.model = WhisperModel(config.WHISPER_MODEL_SIZE, device="cpu", compute_type="int8")
            logger.info("Whisper model loaded successfully.")
        except Exception as e:
            logger.critical(f"Failed to load Whisper model: {e}")
            raise e

    def transcribe(self, audio_path: str) -> dict:
        """
        Transcribes the audio file.
        Returns a dictionary with full text and segments.
        """
        try:
            segments, info = self.model.transcribe(audio_path, beam_size=5)
            
            # segments is a generator, so we must iterate to get results
            # This is blocking, but necessary for getting the full text
            text_segments = []
            full_text = []
            
            for segment in segments:
                text_segments.append({
                    "start": segment.start,
                    "end": segment.end,
                    "text": segment.text
                })
                full_text.append(segment.text)
            
            combined_text = " ".join(full_text).strip()
            
            return {
                "language": info.language,
                "duration": info.duration,
                "text": combined_text,
                "segments": text_segments
            }
        except Exception as e:
            logger.error(f"Transcription failed: {e}")
            raise ValueError(f"Transcription failed: {str(e)}")
