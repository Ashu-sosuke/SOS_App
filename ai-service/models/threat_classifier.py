import torch
from transformers import DistilBertTokenizer, DistilBertForSequenceClassification
import logging
from config import config

logger = logging.getLogger(__name__)

class ThreatClassifier:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model_id = config.THREAT_MODEL_ID
        
        logger.info(f"Loading Threat Classifier: {self.model_id} on {self.device}")
        
        try:
            self.tokenizer = DistilBertTokenizer.from_pretrained(self.model_id)
            self.model = DistilBertForSequenceClassification.from_pretrained(self.model_id)
            self.model.to(self.device)
            self.model.eval()
            
            # Define labels map (In a real scenario, this matches the fine-tuned model's config)
            # For this MVP, we map standard sentiment/outputs to our specific categories
            # ideally we would fine-tune, but here we use zero-shot or mapped logic if generic.
            # Assuming we use a model fine-tuned on emergency data or we map similar concepts.
            # For demonstration, we'll map generic sentiment/toxicity to these labels 
            # OR assume the model output corresponds to these indices.
            
            self.LABELS = ["FALSE_ALARM", "MEDICAL", "FIRE", "ASSAULT", "KIDNAP", "PANIC"]
            
        except Exception as e:
            logger.critical(f"Failed to load Threat Classifier: {e}")
            raise e

    def classify(self, text: str) -> dict:
        """
        Classifies the transcript into emergency categories.
        """
        if not text:
            return {
                "threat_type": "FALSE_ALARM",
                "confidence": 0.0,
                "raw_label": "FALSE_ALARM"
            }
            
        try:
            inputs = self.tokenizer(
                text, 
                return_tensors="pt", 
                truncation=True, 
                padding=True, 
                max_length=512
            ).to(self.device)
            
            with torch.no_grad():
                outputs = self.model(**inputs)
                probs = torch.nn.functional.softmax(outputs.logits, dim=-1)
                
            # Get top prediction
            top_prob, top_idx = torch.max(probs, dim=1)
            
            # Map index to label (Safe lookup)
            idx = top_idx.item()
            label = self.LABELS[idx] if idx < len(self.LABELS) else "UNKNOWN"
            confidence = top_prob.item()
            
            # Keyword boosting (Hybrid approach for reliability)
            # If model is uncertain but keywords are present, override or boost
            # This is crucial for "Explainable AI" in government context
            refined_label = self._keyword_override(text, label, confidence)
            
            return {
                "threat_type": refined_label,
                "confidence": float(round(confidence, 4)),
                "raw_label": label
            }
            
        except Exception as e:
            logger.error(f"Classification failed: {e}")
            return {"threat_type": "FALSE_ALARM", "confidence": 0.0, "error": str(e)}

    def _keyword_override(self, text: str, label: str, confidence: float) -> str:
        text_lower = text.lower()
        
        # Critical keywords that should trigger high alert even if model is unsure
        critical_keywords = {
            "fire": "FIRE",
            "burning": "FIRE",
            "smoke": "FIRE",
            "heart attack": "MEDICAL",
            "ambulance": "MEDICAL",
            "bleeding": "MEDICAL",
            "gun": "ASSAULT",
            "shoot": "ASSAULT",
            "knife": "ASSAULT",
            "kill": "ASSAULT",
            "help me": "PANIC",
            "kidnap": "KIDNAP",
            "taken": "KIDNAP"
        }
        
        # Simple override logic
        for word, override_label in critical_keywords.items():
            if word in text_lower:
                # If the model prediction is weak (<0.7) and we find a strong keyword,
                # we might want to trust the keyword or at least flag it.
                # For this MVP, we return the keyword label if confidence is low.
                if confidence < 0.6:
                    return override_label
                    
        return label
