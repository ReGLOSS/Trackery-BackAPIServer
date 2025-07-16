package com.trackery.trackerybackapiserver.domain.tag.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.trackery.trackerybackapiserver.domain.tag.entity.ImageTag;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.mapper
 * fileName       : TagMapper
 * author         : inari
 * date           : 25. 7. 2.
 * description    : 태그 관련 데이터베이스 매퍼 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.        inari           최초 생성
 * 25. 7. 11.       inari           태그 일괄 삭제시 사용 카운트 일괄 감소 추가
 * 25. 7. 15.       inari           사용하지 않는 매퍼 삭제
 */
@Mapper
public interface TagMapper {

	/**
	 * 태그를 데이터베이스에 삽입합니다.
	 * @param tag 삽입할 태그 정보
	 */
	void insertTag(Tag tag);

	/**
	 * 태그 ID로 태그를 조회합니다.
	 * @param tagId 태그 ID
	 * @return 태그 정보
	 */
	Tag findTagById(@Param("tagId") Long tagId);

	/**
	 * 태그명과 타입으로 태그를 조회합니다.
	 * @param tagName 태그명
	 * @param tagType 태그 타입
	 * @return 태그 정보
	 */
	Tag findTagByNameAndType(@Param("tagName") String tagName, @Param("tagType") TagType tagType);

	/**
	 * 태그명으로 기존 태그를 검색합니다 (시스템 태그 우선).
	 * @param tagName 태그명
	 * @return 기존 태그 (시스템 태그 우선, 없으면 null)
	 */
	Tag findExistingTagByName(@Param("tagName") String tagName);

	/**
	 * 모든 태그 목록을 조회합니다.
	 * @return 모든 태그 목록
	 */
	List<Tag> findAllTags();

	/**
	 * 시스템 태그 목록을 조회합니다.
	 * @return 시스템 태그 목록
	 */
	List<Tag> findSystemTags();

	/**
	 * 이미지 ID로 연결된 태그 목록을 조회합니다.
	 * @param imageId 이미지 ID
	 * @return 이미지에 연결된 태그 목록
	 */
	List<Tag> findTagsByImageId(@Param("imageId") Long imageId);

	/**
	 * 태그 사용 횟수를 증가시킵니다.
	 * @param tagId 태그 ID
	 */
	void incrementTagUseCount(@Param("tagId") Long tagId);

	/**
	 * 태그 사용 횟수를 감소시킵니다.
	 * @param tagId 태그 ID
	 */
	void decrementTagUseCount(@Param("tagId") Long tagId);

	/**
	 * 특정 이미지의 모든 태그 사용 횟수를 배치로 감소시킵니다.
	 * @param imageId 이미지 ID
	 */
	void decrementTagUseCountByImageId(@Param("imageId") Long imageId);

	/**
	 * 사용되지 않는 태그만 안전하게 삭제합니다.
	 * @param tagId 삭제할 태그 ID
	 * @return 삭제된 행 수 (0이면 삭제 실패 - 태그가 사용 중)
	 */
	int deleteUnusedTag(@Param("tagId") Long tagId);

	/**
	 * 태그가 현재 사용 중인지 확인합니다.
	 * @param tagId 태그 ID
	 * @return 사용 중이면 true, 아니면 false
	 */
	boolean isTagInUse(@Param("tagId") Long tagId);

	/**
	 * 이미지-태그 연결 정보를 삽입합니다.
	 * @param imageTag 이미지-태그 연결 정보
	 */
	void insertImageTag(ImageTag imageTag);

	/**
	 * 이미지-태그 연결을 삭제합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 태그 ID
	 */
	void deleteImageTag(@Param("imageId") Long imageId, @Param("tagId") Long tagId);

	/**
	 * 이미지와 연결된 모든 태그 연결을 삭제합니다.
	 * @param imageId 이미지 ID
	 */
	void deleteImageTagsByImageId(@Param("imageId") Long imageId);

	/**
	 * 특정 이미지에 특정 태그가 연결되어 있는지 확인합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 태그 ID
	 * @return 연결되어 있으면 true, 아니면 false
	 */
	boolean isImageTagConnected(@Param("imageId") Long imageId, @Param("tagId") Long tagId);

}
