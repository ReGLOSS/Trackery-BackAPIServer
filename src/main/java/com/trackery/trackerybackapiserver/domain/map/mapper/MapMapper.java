package com.trackery.trackerybackapiserver.domain.map.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.map.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.map.entity.Sido;
import com.trackery.trackerybackapiserver.domain.map.entity.Sigungu;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.map.mapper
 * fileName       : MapMapper
 * author         : inari
 * date           : 25. 5. 16.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 16.        inari       최초 생성
 */
@Mapper
public interface MapMapper {

	List<Sido> findAllSido();
	List<Sigungu> findSigunguBySido(Long sidoId);
	Sigungu findSigunguByCoordinate(CoordinateRequestDto coordinate);
	String findSidoBorderAsGeoJson(Long sidoId);
	String findSigunguBorderAsGeoJson(Long sigunguId);

}
