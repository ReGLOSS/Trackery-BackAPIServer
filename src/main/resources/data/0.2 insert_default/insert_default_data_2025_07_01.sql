-- ========================================
-- DATABASE SCHEMA CHANGE LOG
-- ========================================
-- 인서트전 지오데이터 인서트 필수!!
--
-- 2025-07-01: 최초 생성, 유저 역활 및 지역 기본 태그 추가
-- ========================================


INSERT INTO role (role_id, role_name) VALUES
                                          (1, 'USER'),
                                          (2, 'MANAGER');

-- 시도 기본 태그 인서트 (juso_sido 테이블에서 자동 생성)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at)
SELECT sd_name, 1, 0, NOW()
FROM juso_sido
ORDER BY sd_id;

-- 시군구 기본 태그 인서트 (juso_sigungu 테이블에서 자동 생성)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at)
SELECT sgg_name, 1, 0, NOW()
FROM juso_sigungu
ORDER BY sgg_id;
