import librosa
import numpy as np
import logging

logger = logging.getLogger(__name__)

class AudioStressDetector:
    def __init__(self):
        # Constants for heuristics (calibrated for standard speech)
        self.PITCH_THRESHOLD = 300.0  # Hz, simplistic high pitch threshold
        self.ENERGY_THRESHOLD = 0.05  # RMS energy threshold
        
    def analyze(self, audio_path: str) -> dict:
        """
        Analyzes audio for stress indicators using acoustic features.
        Returns a dictionary with stress score and details.
        """
        try:
            # Load audio (downsample to 16kHz for speed)
            y, sr = librosa.load(audio_path, sr=16000)
            
            # 1. RMS Energy (Loudness/Intensity)
            rms = librosa.feature.rms(y=y)
            avg_energy = np.mean(rms)
            
            # 2. Pitch (Fundamental Frequency - F0) using pYIN
            # This is slower but accurate. For speed, we might skip steps, 
            # but for "explainability" pitch is key.
            f0, voiced_flag, voiced_probs = librosa.pyin(
                y, fmin=librosa.note_to_hz('C2'), fmax=librosa.note_to_hz('C7')
            )
            
            # Filter distinct pitches
            valid_pitches = f0[~np.isnan(f0)]
            avg_pitch = np.mean(valid_pitches) if len(valid_pitches) > 0 else 0
            
            # 3. Speech Rate / Zero Crossing Rate (Agitation)
            zcr = librosa.feature.zero_crossing_rate(y)
            avg_zcr = np.mean(zcr)
            
            # heuristic scoring (0.0 to 1.0)
            # High pitch + High Energy + Fast Speech = Panic
            
            score = 0.0
            explanations = []

            # Energy Contribution (0.4 max)
            if avg_energy > self.ENERGY_THRESHOLD:
                score += 0.4
                explanations.append("High voice intensity detected")
            elif avg_energy > self.ENERGY_THRESHOLD * 0.5:
                score += 0.2
            
            # Pitch Contribution (0.4 max)
            if avg_pitch > self.PITCH_THRESHOLD:
                score += 0.4
                explanations.append("High pitch/screaming detected")
            elif avg_pitch > self.PITCH_THRESHOLD * 0.7:
                score += 0.2
                
            # ZCR/Agitation (0.2 max)
            if avg_zcr > 0.1: # Threshold for noisy/breathless speech
                score += 0.2
                explanations.append("Rapid/agitated speech pattern")
                
            return {
                "stress_score": round(min(score, 1.0), 2),
                "details": {
                    "avg_pitch_hz": float(round(avg_pitch, 2)),
                    "avg_energy": float(round(avg_energy, 4)),
                    "metrics": explanations
                }
            }
            
        except Exception as e:
            logger.error(f"Emotion analysis failed: {e}")
            # Fallback for error safety
            return {"stress_score": 0.0, "error": str(e), "details": {}}
