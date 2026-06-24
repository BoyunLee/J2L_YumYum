drop schema if exists yumyum;
create schema yumyum;
use yumyum;

-- =========================================================
-- FOOD NUTRITION
-- 기존 테이블 유지 + 바코드 컬럼 추가
-- =========================================================

CREATE TABLE IF NOT EXISTS food_nutrition (
    food_code         VARCHAR(50)   NOT NULL COMMENT '식품코드',
    food_name         VARCHAR(200)  NOT NULL COMMENT '식품명',
    category          VARCHAR(100)  NULL COMMENT '식품대분류명',
    weight            VARCHAR(50)   NULL COMMENT '식품중량(표시단위 포함)',
    energy_kcal       DECIMAL(10,2) NULL COMMENT '에너지(kcal)',
    protein_g         DECIMAL(10,2) NULL COMMENT '단백질(g)',
    fat_g             DECIMAL(10,2) NULL COMMENT '지방(g)',
    carbohydrate_g    DECIMAL(10,2) NULL COMMENT '탄수화물(g)',
    sugar_g           DECIMAL(10,2) NULL COMMENT '당류(g)',
    sodium_mg         DECIMAL(10,2) NULL COMMENT '나트륨(mg)',
    cholesterol_mg    DECIMAL(10,2) NULL COMMENT '콜레스테롤(mg)',
    saturated_fat_g   DECIMAL(10,2) NULL COMMENT '포화지방산(g)',
    trans_fat_g       DECIMAL(10,2) NULL COMMENT '트랜스지방산(g)',
    caffeine_mg       DECIMAL(10,2) NULL COMMENT '카페인(mg)',
    barcode           VARCHAR(50) NULL COMMENT '대표 바코드',
    PRIMARY KEY (food_code),
    FULLTEXT KEY idx_ft_food_name (food_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='음식 영양정보 테이블';


-- =========================================================
-- USERS
-- =========================================================

CREATE TABLE IF NOT EXISTS users (
    user_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider           ENUM(
                            'GOOGLE',
                            'KAKAO',
                            'NAVER'
                        ) NOT NULL COMMENT 'OAuth 제공자',
    provider_user_id   VARCHAR(255) NOT NULL COMMENT 'OAuth 제공자의 사용자 고유 ID',
    email              VARCHAR(255) NULL COMMENT 'OAuth 제공자로부터 받은 이메일',
    nickname           VARCHAR(100) NOT NULL COMMENT '서비스 내 닉네임',
    profile_image_url  VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    role               ENUM(
                            'USER',
                            'GUEST'
                        ) NOT NULL DEFAULT 'USER' COMMENT '사용자 권한',
    gender             ENUM('M','F','OTHER') NULL,
    birth_date         DATE NULL,
    height_cm          DECIMAL(5,2) NULL,
    weight_kg          DECIMAL(5,2) NULL,
    activity_level     ENUM(
                            'LOW',
                            'MEDIUM',
                            'HIGH'
                        ) NULL DEFAULT 'MEDIUM',
    last_login_at      DATETIME NULL COMMENT '마지막 로그인 시각',
    deleted_at         DATETIME NULL COMMENT '회원 탈퇴 시각',
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원';


-- =========================================================
-- REFRIGERATORS
-- =========================================================

CREATE TABLE IF NOT EXISTS refrigerators (
    refrigerator_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT NOT NULL,
    refrigerator_name  VARCHAR(100) NOT NULL DEFAULT '내 냉장고',
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_refrigerators_user (user_id),
    CONSTRAINT fk_refrigerators_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='냉장고';


-- =========================================================
-- REFRIGERATOR ITEMS
-- =========================================================

CREATE TABLE IF NOT EXISTS refrigerator_items (
    item_id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    refrigerator_id        BIGINT NOT NULL,
    food_code              VARCHAR(50) NULL COMMENT '수기 등록 시 영양정보와 연결되지 않을 수 있음',
    item_name              VARCHAR(200) NOT NULL COMMENT '사용자 표시용 이름',
    category               VARCHAR(30) NOT NULL DEFAULT 'ETC',
    quantity               DECIMAL(10,2) NOT NULL DEFAULT 1,
    quantity_unit          VARCHAR(30) NULL COMMENT '개, g, ml 등',
    purchase_date          DATE NULL,
    expiration_date        DATE NULL,
    storage_location       ENUM(
                                'REFRIGERATOR',
                                'FREEZER',
                                'ROOM_TEMPERATURE'
                            ) DEFAULT 'REFRIGERATOR',
    registration_type      ENUM(
                                'OCR',
                                'BARCODE',
                                'MANUAL'
                            ) NOT NULL,
    memo                   VARCHAR(500) NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME NOT NULL
                                DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_refrigerator
        FOREIGN KEY (refrigerator_id)
        REFERENCES refrigerators(refrigerator_id),
    CONSTRAINT fk_items_food
        FOREIGN KEY (food_code)
        REFERENCES food_nutrition(food_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='냉장고 재고';


-- =========================================================
-- REFRIGERATOR ITEM LOGS
-- =========================================================

CREATE TABLE IF NOT EXISTS refrigerator_item_logs (
    log_id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id                BIGINT NOT NULL,
    action_type            ENUM(
                                'CREATE',
                                'UPDATE',
                                'DELETE'
                            ) NOT NULL,
    raw_ocr_text           TEXT NULL,
    barcode_raw            VARCHAR(100) NULL,
    before_data            JSON NULL,
    after_data             JSON NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_logs_item
        FOREIGN KEY (item_id)
        REFERENCES refrigerator_items(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='재고 변경 로그';


-- =========================================================
-- RECIPES
-- =========================================================

CREATE TABLE IF NOT EXISTS recipes (
    recipe_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_name            VARCHAR(200) NOT NULL,
    description            TEXT NULL,
    cooking_time_min       INT NULL,
    serving_size           INT NOT NULL DEFAULT 1 COMMENT '기본 인분',
    difficulty             ENUM(
                                'EASY',
                                'NORMAL',
                                'HARD'
                            ) DEFAULT 'NORMAL',
    recipe_image_url       VARCHAR(500) NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT KEY idx_ft_recipe_name (recipe_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='레시피';


-- =========================================================
-- RECIPE INGREDIENTS
-- =========================================================

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    recipe_ingredient_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id              BIGINT NOT NULL,
    food_code              VARCHAR(50) NOT NULL,
    required_quantity      DECIMAL(10,2) NOT NULL,
    quantity_unit          VARCHAR(30) NULL,
    is_required            BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_recipe_ingredients_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(recipe_id),
    CONSTRAINT fk_recipe_ingredients_food
        FOREIGN KEY (food_code)
        REFERENCES food_nutrition(food_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='레시피 재료';


-- =========================================================
-- RECIPE RECOMMENDATION LOGS
-- =========================================================

CREATE TABLE IF NOT EXISTS recipe_recommendation_logs (
    recommendation_log_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT NOT NULL,
    recipe_id              BIGINT NOT NULL,
    recommendation_reason  ENUM(
                                'EXPIRATION_PRIORITY',
                                'HIGH_QUANTITY_PRIORITY',
                                'NUTRITION_BALANCE'
                            ) NOT NULL,
    recommendation_score   DECIMAL(10,2) NOT NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommend_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),
    CONSTRAINT fk_recommend_logs_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='레시피 추천 로그';


-- =========================================================
-- MEAL LOGS
-- =========================================================

CREATE TABLE IF NOT EXISTS meal_logs (
    meal_log_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT NOT NULL,
    log_type               ENUM(
                                'CONSUMED',
                                'RECOMMENDATION'
                            ) NOT NULL DEFAULT 'CONSUMED',
    meal_type              ENUM(
                                'BREAKFAST',
                                'LUNCH',
                                'DINNER',
                                'SNACK'
                            ) NULL,
    eaten_at               DATETIME NULL,
    memo                   VARCHAR(500) NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='식단 기록';


-- =========================================================
-- MEAL LOG ITEMS
-- =========================================================

CREATE TABLE IF NOT EXISTS meal_log_items (
    meal_log_item_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_log_id            BIGINT NOT NULL,
    item_type              ENUM(
                                'CONSUMED',
                                'RECOMMENDATION'
                            ) NOT NULL DEFAULT 'CONSUMED',
    food_code              VARCHAR(50) NULL,
    item_name              VARCHAR(200) NULL,
    quantity               DECIMAL(10,2) NOT NULL DEFAULT 1,
    quantity_unit          VARCHAR(30) NULL,
    recipe_payload         JSON NULL,
    CONSTRAINT fk_meal_items_meal_log
        FOREIGN KEY (meal_log_id)
        REFERENCES meal_logs(meal_log_id),
    CONSTRAINT fk_meal_items_food
        FOREIGN KEY (food_code)
        REFERENCES food_nutrition(food_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='식단 상세';


-- =========================================================
-- NOTIFICATIONS
-- =========================================================

CREATE TABLE IF NOT EXISTS notifications (
    notification_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT NOT NULL,
    refrigerator_item_id   BIGINT NULL,
    notification_type      ENUM(
                                'EXPIRATION',
                                'MEAL_REMINDER',
                                'ADMIN_PUSH'
                            ) NOT NULL,
    title                  VARCHAR(200) NOT NULL,
    content                TEXT NOT NULL,
    expiration_status      ENUM('EXPIRING_SOON', 'EXPIRED') NULL,
    reference_date         DATE NULL,
    is_read                BOOLEAN NOT NULL DEFAULT FALSE,
    scheduled_at           DATETIME NULL,
    sent_at                DATETIME NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),
    CONSTRAINT fk_notifications_refrigerator_item
        FOREIGN KEY (refrigerator_item_id)
        REFERENCES refrigerator_items(item_id)
        ON DELETE SET NULL,
    CONSTRAINT uk_expiration_notification
        UNIQUE (user_id, refrigerator_item_id, expiration_status, reference_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='알림';


-- =========================================================
-- USER PUSH TOKENS
-- =========================================================

CREATE TABLE IF NOT EXISTS user_push_tokens (
    push_token_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT NOT NULL,
    device_type            ENUM(
                                'ANDROID',
                                'IOS',
                                'WEB'
                            ) NOT NULL,
    push_token             VARCHAR(500) NOT NULL,
    is_active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_push_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),
    UNIQUE KEY uk_push_token (push_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='푸쉬 토큰';


-- =========================================================
-- ADMIN USERS
-- =========================================================

CREATE TABLE IF NOT EXISTS admin_users (
    admin_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id               VARCHAR(100) NOT NULL UNIQUE,
    password_hash          VARCHAR(255) NOT NULL,
    role                   ENUM(
                                'SUPER_ADMIN',
                                'OPERATOR'
                            ) NOT NULL DEFAULT 'OPERATOR',
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자';


-- =========================================================
-- API USAGE LOGS
-- =========================================================

CREATE TABLE IF NOT EXISTS api_usage_logs (
    api_usage_log_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT NULL,
    api_type               ENUM('OCR', 'BARCODE', 'RECIPE_RECOMMENDATION') NOT NULL,
    success                BOOLEAN NOT NULL DEFAULT TRUE,
    duration_ms            BIGINT NOT NULL DEFAULT 0,
    error_code             VARCHAR(50) NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_api_usage_created_type (created_at, api_type),
    CONSTRAINT fk_api_usage_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='외부 API 호출 이력';


-- =========================================================
-- BATCH JOB HISTORY
-- =========================================================

CREATE TABLE IF NOT EXISTS batch_job_history (
    batch_history_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name               VARCHAR(100) NOT NULL,
    trigger_type           ENUM('SCHEDULED', 'MANUAL') NOT NULL,
    status                 ENUM('RUNNING', 'SUCCESS', 'FAILED') NOT NULL,
    processed_count        INT NOT NULL DEFAULT 0,
    duration_ms            BIGINT NULL,
    started_at             DATETIME NOT NULL,
    finished_at            DATETIME NULL,
    error_message          VARCHAR(500) NULL,
    KEY idx_batch_started_at (started_at),
    KEY idx_batch_job_status (job_name, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='배치 작업 실행 이력';


-- =========================================================
-- ADMIN LOGS
-- =========================================================

CREATE TABLE IF NOT EXISTS admin_logs (
    admin_log_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id               BIGINT NOT NULL,
    action_type            VARCHAR(100) NOT NULL,
    target_type            VARCHAR(100) NULL,
    target_id              VARCHAR(100) NULL,
    action_detail          TEXT NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_logs_admin
        FOREIGN KEY (admin_id)
        REFERENCES admin_users(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자 로그';


-- =========================================================
-- ADMIN PUSH HISTORY
-- =========================================================

CREATE TABLE IF NOT EXISTS admin_push_history (
    push_history_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id               BIGINT NOT NULL,
    title                  VARCHAR(200) NOT NULL,
    content                TEXT NOT NULL,
    target_condition       JSON NULL COMMENT '대상 조건',
    sent_count             INT NOT NULL DEFAULT 0,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_push_history_admin
        FOREIGN KEY (admin_id)
        REFERENCES admin_users(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자 푸쉬 발송 이력';
