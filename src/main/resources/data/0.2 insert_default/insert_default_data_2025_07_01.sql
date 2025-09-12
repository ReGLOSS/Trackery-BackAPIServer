-- ========================================
-- DATABASE SCHEMA CHANGE LOG
-- ========================================
-- 인서트전 지오데이터 인서트 필수!!
-- 지오 데이터 인서트전 SET GLOBAL max_allowed_packet = 256 * 1024 * 1024; 설정 필요
-- mysql 설치후 SELECT * FROM mysql.time_zone_name LIMIT 5; 해서 테이블이 비어있을 경우 추가해야함 (도커 mysql 이미지엔 포함)
-- 2025-07-01: 최초 생성, 유저 역활 및 지역 기본 태그 추가
-- ========================================


INSERT INTO role (role_id, role_name) VALUES
                                          (1, 'USER'),
                                          (2, 'MANAGER'),
                                          (3, 'ADMIN');

-- tag_type: 1 (시도)

-- 시도 기본 태그 인서트 (juso_sido 테이블에서 자동 생성)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at)
SELECT sd_name, 1, 0, NOW()
FROM juso_sido
ORDER BY sd_id;

-- tag_type: 2 (시군구)

-- 시군구 기본 태그 인서트 (juso_sigungu 테이블에서 자동 생성)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at)
SELECT sgg_name, 2, 0, NOW()
FROM juso_sigungu
ORDER BY sgg_id;

-- tag_type: 3 (계절)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at) VALUES
                                                                    ('봄', 3, 0, NOW()),
                                                                    ('여름', 3, 0, NOW()),
                                                                    ('가을', 3, 0, NOW()),
                                                                    ('겨울', 3, 0, NOW());

-- tag_type: 4 (시간)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at) VALUES
                                                                    ('아침', 4, 0, NOW()),
                                                                    ('점심', 4, 0, NOW()),
                                                                    ('저녁', 4, 0, NOW()),
                                                                    ('밤', 4, 0, NOW());

-- tag_type: 5 (날씨)
INSERT INTO tag (tag_name, tag_type, tag_use_count, created_at) VALUES
                                                                    ('맑음', 5, 0, NOW()),
                                                                    ('흐림', 5, 0, NOW()),
                                                                    ('비', 5, 0, NOW()),
                                                                    ('눈', 5, 0, NOW());
