from pydantic import BaseModel, HttpUrl, Field, validator
from typing import Optional, List, Dict, Any
from enum import Enum

class RecommendedAction(str, Enum):
    NOTIFY = "NOTIFY"
    ESCALATE = "ESCALATE"
    EMERGENCY_DISPATCH = "EMERGENCY_DISPATCH"

class IncidentInput(BaseModel):
    incidentId: str = Field(..., description="Unique ID of the incident")
    audioUrl: str = Field(..., description="Signed URL to the audio file")
    latitude: Optional[float] = Field(None, ge=-90, le=90)
    longitude: Optional[float] = Field(None, ge=-180, le=180)
    timestamp: int = Field(..., description="Unix timestamp")
    
    @validator('audioUrl')
    def validate_url(cls, v):
        if not v.startswith(('http://', 'https://')):
            raise ValueError('audioUrl must be a valid HTTP/HTTPS URL')
        return v

class IncidentOutput(BaseModel):
    incidentId: str
    transcript: str
    stressScore: float
    threatType: str
    severityScore: float
    finalSeverity: str
    confidence: float
    recommendedAction: RecommendedAction
    modelVersion: str = "v1.0.0"
    details: Optional[Dict[str, Any]] = None
