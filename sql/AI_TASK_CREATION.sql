-- AI 任务表（对应 domain: AiTask）
-- 约束说明：
-- 1) id 不自增，必须由后端生成并写入
-- 2) 只有 created_at / updated_at 由数据库自动生成
-- 3) start_at / finished_at 由后端填入，允许为空

CREATE TABLE IF NOT EXISTS AI_TASK_CREATION (
    id           BIGINT PRIMARY KEY,
    owner_id     BIGINT       ,
    owner_name   VARCHAR(128) ,

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    task_name    VARCHAR(255) NOT NULL,
    task_type    VARCHAR(64)  NOT NULL,

    biz_status   VARCHAR(64),
    biz_type     VARCHAR(64),
    biz_no       VARCHAR(128),
    sub_biz_no   VARCHAR(128),

    task_status  VARCHAR(64)  NOT NULL,
    exec_status  VARCHAR(64)  NOT NULL,

    priority     INTEGER      NOT NULL DEFAULT 100,
    sharding     INTEGER      NOT NULL DEFAULT 0,

    start_at     TIMESTAMPTZ  NULL,
    finished_at  TIMESTAMPTZ  NULL,

    biz_params   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    biz_exec_info JSONB       NOT NULL DEFAULT '{}'::jsonb,
    biz_result   JSONB        NOT NULL DEFAULT '{}'::jsonb,

    sys_params   JSONB,
    exec_info    JSONB,
    sys_result   JSONB
);

-- 时间戳自动填充（处理“JOOQ 上传实体字段为 null”场景）
CREATE OR REPLACE FUNCTION fn_ai_task_creation_fill_timestamps()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.created_at IS NULL THEN
            NEW.created_at := CURRENT_TIMESTAMP;
        END IF;
        IF NEW.updated_at IS NULL THEN
            NEW.updated_at := CURRENT_TIMESTAMP;
        END IF;
    ELSIF TG_OP = 'UPDATE' THEN
        -- 每次更新自动刷新 updated_at
        NEW.updated_at := CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_ai_task_creation_fill_timestamps ON AI_TASK_CREATION;
CREATE TRIGGER trg_ai_task_creation_fill_timestamps
BEFORE INSERT OR UPDATE ON AI_TASK_CREATION
FOR EACH ROW
EXECUTE FUNCTION fn_ai_task_creation_fill_timestamps();

-- 常用查询索引
CREATE INDEX IF NOT EXISTS idx_ai_task_creation_owner_id ON AI_TASK_CREATION(owner_id);
CREATE INDEX IF NOT EXISTS idx_ai_task_creation_task_status ON AI_TASK_CREATION(task_status);
CREATE INDEX IF NOT EXISTS idx_ai_task_creation_exec_status ON AI_TASK_CREATION(exec_status);
CREATE INDEX IF NOT EXISTS idx_ai_task_creation_biz ON AI_TASK_CREATION(biz_type, biz_no, sub_biz_no);
CREATE INDEX IF NOT EXISTS idx_ai_task_creation_created_at ON AI_TASK_CREATION(created_at DESC);
