package info.jemsit.media_service.mapper;

import info.jemsit.common.dto.response.media.SessionResponseDTO;
import info.jemsit.media_service.data.model.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    SessionResponseDTO toDto(Session session);
}
