package info.jemsit.media_service.mapper;

import info.jemsit.media_service.data.model.Session;
import info.jemsit.media_service.service.SessionResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    SessionResponseDTO toDto(Session session);
}
