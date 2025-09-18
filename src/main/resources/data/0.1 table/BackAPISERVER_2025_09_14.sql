-- ========================================
-- DATABASE SCHEMA CHANGE LOG
-- ========================================
-- 2025-05-29: 초기 스키마 생성
-- 2025-07-01: coord_zone 테이블 삭제, spring_session 테이블 삭제, 게시물이외 erd에 적용 완료,
--             태그-이미지 중간 테이블 추가로 다대다 관계로 변경
-- 2025-08-14: Image 테이블에서 imageFile, imageType 삭제
-- 2025-09-14: 정지 이력 테이블 추가
-- ========================================

create table juso_sido
(
    sd_id     bigint       not null
        primary key,
    sd_name   varchar(50)  not null,
    sd_border multipolygon not null
);

create spatial index idx_sd_border
    on juso_sido (sd_border);

create table juso_sigungu
(
    sgg_id     bigint       not null
        primary key,
    sd_id      bigint       not null,
    sgg_name   varchar(50)  not null,
    sgg_border multipolygon not null,
    constraint juso_sigungu_ibfk_1
        foreign key (sd_id) references juso_sido (sd_id)
);

create table coord_poi
(
    coord_point_id     bigint auto_increment
        primary key,
    coord_point_name   varchar(255) null,
    coord_point_detail text         null,
    coord_point_point  point        null,
    coord_point_type   tinyint      null,
    first_reg_dt       datetime     null,
    last_mod_dt        datetime     null,
    sgg_id             bigint       not null,
    constraint coord_poi_ibfk_1
        foreign key (sgg_id) references juso_sigungu (sgg_id)
);

create spatial index idx_sgg_border
    on juso_sigungu (sgg_border);

create table role
(
    role_id   bigint auto_increment
        primary key,
    role_name varchar(50) not null
);

create table tag
(
    tag_id        bigint auto_increment
        primary key,
    tag_name      varchar(50)       not null,
    tag_type      tinyint default 0 null,
    tag_use_count bigint  default 0 null,
    created_at    datetime          not null
);

create table user
(
    user_id        bigint auto_increment
        primary key,
    email          varchar(255) not null,
    username       varchar(255) not null,
    nickname       varchar(50)  not null,
    password       varchar(255) not null,
    start_date     datetime     not null,
    status         tinyint      not null,
    last_login     datetime     not null,
    coord_point_id bigint       null,
    user_profile   varchar(255) null,
    salt           varchar(255) not null,
    constraint user_ibfk_1
        foreign key (coord_point_id) references coord_poi (coord_point_id)
);

create table image
(
    image_id          bigint auto_increment
        primary key,
    coord_point_id    bigint            not null,
    image_name        varchar(255)      not null,
    is_public         tinyint           not null,
    is_deleted        tinyint(1)        not null,
    image_contents    varchar(255)      null,
    image_date        datetime          not null,
    image_reg_date    datetime          not null,
    user_id           bigint            null,
    processing_status tinyint default 0 not null,
    constraint image_ibfk_1
        foreign key (coord_point_id) references coord_poi (coord_point_id),
    constraint image_ibfk_2
        foreign key (user_id) references user (user_id)
);

create table album
(
    album_id           bigint auto_increment
        primary key,
    user_id            bigint            not null,
    album_title        varchar(255)      not null,
    album_description  text              null,
    thumbnail_image_id bigint            null,
    album_reg_date     datetime          not null,
    album_mod_date     datetime          not null,
    is_public          tinyint default 0 null,
    is_deleted         tinyint default 0 not null,
    constraint album_ibfk_1
        foreign key (user_id) references user (user_id),
    constraint album_ibfk_2
        foreign key (thumbnail_image_id) references image (image_id)
);

create index thumbnail_image_id
    on album (thumbnail_image_id);

create table album_image
(
    album_image_id bigint auto_increment
        primary key,
    album_id       bigint not null,
    image_id       bigint not null,
    constraint album_image_ibfk_1
        foreign key (album_id) references album (album_id),
    constraint album_image_ibfk_2
        foreign key (image_id) references image (image_id)
);

create table image_tag
(
    image_tag_id bigint auto_increment
        primary key,
    image_id     bigint   not null,
    tag_id       bigint   not null,
    created_at   datetime null,
    constraint uk_image_tag
        unique (image_id, tag_id),
    constraint image_tag_ibfk_1
        foreign key (image_id) references image (image_id)
            on delete cascade,
    constraint image_tag_ibfk_2
        foreign key (tag_id) references tag (tag_id)
            on delete cascade
);

create index idx_image_tag_image_id
    on image_tag (image_id);

create index idx_image_tag_tag_id
    on image_tag (tag_id);

create table `like`
(
    like_id     bigint auto_increment
        primary key,
    user_id     bigint                   not null,
    target_id   bigint                   not null,
    target_type enum ('POST', 'COMMENT') not null,
    constraint like_ibfk_1
        foreign key (user_id) references user (user_id)
);

create table line
(
    line_id  bigint auto_increment
        primary key,
    album_id bigint not null,
    polyline text   not null,
    constraint line_ibfk_1
        foreign key (album_id) references album (album_id)
);

create table notification
(
    notification_id bigint auto_increment
        primary key,
    user_id         bigint     not null,
    noti_type       tinyint    null,
    noti_message    text       null,
    noti_pubtime    datetime   null,
    noti_readtime   datetime   null,
    noti_read_yn    tinyint(1) null,
    constraint notification_ibfk_1
        foreign key (user_id) references user (user_id)
);

create table post
(
    post_id        bigint auto_increment
        primary key,
    user_id        bigint   not null,
    first_reg_dt   datetime not null,
    last_mod_dt    datetime not null,
    contents       text     null,
    is_deleted     tinyint  not null,
    coord_point_id bigint   not null,
    tag            text     null,
    constraint post_ibfk_1
        foreign key (user_id) references user (user_id),
    constraint post_ibfk_2
        foreign key (coord_point_id) references coord_poi (coord_point_id)
);

create table comment
(
    comment_id   bigint auto_increment
        primary key,
    user_id      bigint   not null,
    post_id      bigint   not null,
    first_reg_dt datetime not null,
    last_mod_dt  datetime not null,
    contents     text     not null,
    is_deleted   tinyint  not null,
    parent_id    bigint   null,
    depth        int      not null,
    sequence     int      not null,
    constraint comment_ibfk_1
        foreign key (user_id) references user (user_id),
    constraint comment_ibfk_2
        foreign key (post_id) references post (post_id),
    constraint comment_ibfk_3
        foreign key (parent_id) references comment (comment_id)
);

create table comment_like
(
    comment_like_id bigint auto_increment
        primary key,
    like_id         bigint not null,
    comment_id      bigint not null,
    constraint comment_like_ibfk_1
        foreign key (like_id) references `like` (like_id),
    constraint comment_like_ibfk_2
        foreign key (comment_id) references comment (comment_id)
);

create table post_image
(
    post_image_id bigint auto_increment
        primary key,
    post_id       bigint     not null,
    image_id      bigint     not null,
    sequence      int        not null,
    is_deleted    tinyint(1) not null,
    constraint post_image_ibfk_1
        foreign key (post_id) references post (post_id),
    constraint post_image_ibfk_2
        foreign key (image_id) references image (image_id)
);

create table post_like
(
    post_like_id bigint auto_increment
        primary key,
    like_id      bigint not null,
    post_id      bigint not null,
    constraint post_like_ibfk_1
        foreign key (like_id) references `like` (like_id),
    constraint post_like_ibfk_2
        foreign key (post_id) references post (post_id)
);

create table user_oauth
(
    oauth_id         bigint auto_increment
        primary key,
    user_id          bigint      not null,
    provider         varchar(50) not null,
    provider_user_id varchar(50) not null,
    constraint user_oauth_ibfk_1
        foreign key (user_id) references user (user_id)
);

create table user_role
(
    user_role_id bigint auto_increment
        primary key,
    user_id      bigint not null,
    role_id      bigint not null,
    constraint user_role_ibfk_1
        foreign key (user_id) references user (user_id),
    constraint user_role_ibfk_2
        foreign key (role_id) references role (role_id)
);

create table user_suspension
(
    suspension_id   bigint auto_increment
        primary key,
    user_id         bigint                                not null,
    suspension_type tinyint                               not null,
    start_date      date                                  not null,
    end_date        date                                  null,
    reason          varchar(500)                          null,
    admin_id        bigint                                not null,
    action_type     varchar(20)                           not null,
    is_active       tinyint default 1                     null,
    created_at      datetime default (CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul'))   null,
    updated_at      datetime default (CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul'))   null on update (CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')),
    constraint user_suspension_ibfk_1
        foreign key (user_id) references user (user_id)
);

create index idx_admin_date
    on user_suspension (admin_id, created_at);

create index idx_end_date
    on user_suspension (end_date);

create index idx_user_active
    on user_suspension (user_id, is_active);
