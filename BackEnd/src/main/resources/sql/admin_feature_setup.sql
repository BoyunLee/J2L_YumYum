USE yumyum;

-- 기존 DB에 관리자 기능을 추가할 때 한 번 실행하세요.
CREATE TABLE IF NOT EXISTS api_usage_logs (
    api_usage_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    api_type ENUM('OCR', 'BARCODE', 'RECIPE_RECOMMENDATION') NOT NULL,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    error_code VARCHAR(50) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_api_usage_created_type (created_at, api_type),
    CONSTRAINT fk_api_usage_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='외부 API 호출 이력';

CREATE TABLE IF NOT EXISTS batch_job_history (
    batch_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    trigger_type ENUM('SCHEDULED', 'MANUAL') NOT NULL,
    status ENUM('RUNNING', 'SUCCESS', 'FAILED') NOT NULL,
    processed_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT NULL,
    started_at DATETIME NOT NULL,
    finished_at DATETIME NULL,
    error_message VARCHAR(500) NULL,
    KEY idx_batch_started_at (started_at),
    KEY idx_batch_job_status (job_name, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='배치 작업 실행 이력';

-- 최초 관리자는 ADMIN_BOOTSTRAP_* 환경변수를 통해 애플리케이션 시작 시 생성됩니다.
-- 필요하면 BCrypt 해시를 준비하여 아래 형식으로 직접 추가할 수도 있습니다.
-- INSERT INTO admin_users (login_id, password_hash, role)
-- VALUES ('아이디', '$2a$12$BCrypt로_생성한_해시', 'OPERATOR');
