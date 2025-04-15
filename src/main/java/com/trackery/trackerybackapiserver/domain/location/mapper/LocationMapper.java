package com.trackery.trackerybackapiserver.domain.location.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.mapper
 * fileName       : LocationMapper
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */
@Mapper
public interface LocationMapper{
	Optional<JusoSigungu> getSigungu(CoordinateDto coordinateDto);
}
