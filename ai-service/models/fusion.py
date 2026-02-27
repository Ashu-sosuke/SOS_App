from config import config

class FusionEngine:
    @staticmethod
    def compute_severity(stress_score: float, threat_data: dict, keyword_score: float = 0.0, location_risk: float = 0.0) -> dict:
        """
        Computes the final severity score based on weighted fusion.
        
        Formula:
        0.4 * stress + 0.3 * threat + 0.2 * keyword + 0.1 * location
        
        Args:
            stress_score: 0.0 to 1.0 (from audio analysis)
            threat_data: Dict containing 'threat_type' and 'confidence'
            keyword_score: 0.0 to 1.0 (derived from keyword presence or manual boost)
            location_risk: 0.0 to 1.0 (external input, default 0)
            
        Returns:
            Dict with final_score, severity_level, and recommended_action.
        """
        
        # Threat score mapping (0.0 to 1.0) based on threat type severity
        THREAT_SEVERITY_MAP = {
            "ASSAULT": 1.0,
            "KIDNAP": 1.0,
            "FIRE": 0.9,
            "PANIC": 0.8,
            "MEDICAL": 0.7,
            "FALSE_ALARM": 0.0,
            "UNKNOWN": 0.3
        }
        
        threat_type = threat_data.get("threat_type", "UNKNOWN")
        threat_confidence = threat_data.get("confidence", 0.0)
        
        # Base threat score depends on the TYPE of threat detected
        base_threat_score = THREAT_SEVERITY_MAP.get(threat_type, 0.3)
        
        # Adjust by confidence (if model is unsure, lower the impact slightly, but keep high for critical classes)
        # We don't want to zero it out if confidence is low but it's "ASSAULT"
        threat_score = base_threat_score * (0.5 + 0.5 * threat_confidence)
        
        # Calculate Weighted Sum
        final_score = (
            (config.WEIGHT_STRESS * stress_score) +
            (config.WEIGHT_THREAT * threat_score) +
            (config.WEIGHT_KEYWORD * keyword_score) + 
            (config.WEIGHT_LOCATION * location_risk)
        )
        
        # Clamp to 0-1
        final_score = min(max(final_score, 0.0), 1.0)
        
        # Determine Severity Level
        if final_score >= 0.8:
            severity = "CRITICAL"
            action = "EMERGENCY_DISPATCH"
        elif final_score >= 0.6:
            severity = "HIGH"
            action = "EMERGENCY_DISPATCH"
        elif final_score >= 0.4:
            severity = "MEDIUM"
            action = "ESCALATE"
        else:
            severity = "LOW"
            action = "NOTIFY"
            
        return {
            "final_score": round(final_score, 2),
            "severity_level": severity,
            "recommended_action": action,
            "breakdown": {
                "stress_contribution": round(config.WEIGHT_STRESS * stress_score, 2),
                "threat_contribution": round(config.WEIGHT_THREAT * threat_score, 2),
                "keyword_contribution": round(config.WEIGHT_KEYWORD * keyword_score, 2),
                "location_contribution": round(config.WEIGHT_LOCATION * location_risk, 2)
            }
        }
