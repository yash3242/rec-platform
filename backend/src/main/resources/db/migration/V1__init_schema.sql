CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_id BIGINT NOT NULL REFERENCES roles(id),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE assets (
  id BIGSERIAL PRIMARY KEY,
  asset_code VARCHAR(60) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  energy_source VARCHAR(40) NOT NULL,
  location VARCHAR(160),
  capacity_mw DOUBLE PRECISION,
  status VARCHAR(40) NOT NULL,
  owner_id BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE generation_logs (
  id BIGSERIAL PRIMARY KEY,
  asset_id BIGINT NOT NULL REFERENCES assets(id),
  generation_date DATE NOT NULL,
  energy_source VARCHAR(40) NOT NULL,
  energy_quantity_mwh NUMERIC(14,3) NOT NULL CHECK (energy_quantity_mwh > 0),
  vintage_year INTEGER NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE recs (
  id BIGSERIAL PRIMARY KEY,
  rec_code VARCHAR(50) NOT NULL UNIQUE,
  generation_log_id BIGINT NOT NULL REFERENCES generation_logs(id),
  asset_id BIGINT NOT NULL REFERENCES assets(id),
  energy_source VARCHAR(40) NOT NULL,
  vintage_year INTEGER NOT NULL,
  energy_quantity_mwh NUMERIC(14,3) NOT NULL CHECK (energy_quantity_mwh > 0),
  certificate_quantity INTEGER NOT NULL CHECK (certificate_quantity > 0),
  status VARCHAR(30) NOT NULL,
  owner_id BIGINT NOT NULL REFERENCES users(id),
  listed_at TIMESTAMP,
  transferred_at TIMESTAMP,
  retired_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE status_history (
  id BIGSERIAL PRIMARY KEY,
  resource_type VARCHAR(30) NOT NULL,
  resource_id BIGINT NOT NULL,
  old_status VARCHAR(40),
  new_status VARCHAR(40) NOT NULL,
  changed_by BIGINT NOT NULL REFERENCES users(id),
  comment VARCHAR(500),
  changed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_assets_owner_id ON assets(owner_id);
CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_generation_logs_asset_id ON generation_logs(asset_id);
CREATE INDEX idx_generation_logs_status ON generation_logs(status);
CREATE INDEX idx_recs_owner_id ON recs(owner_id);
CREATE INDEX idx_recs_status ON recs(status);
CREATE INDEX idx_recs_energy_source ON recs(energy_source);
CREATE INDEX idx_recs_vintage_year ON recs(vintage_year);
CREATE INDEX idx_status_history_resource ON status_history(resource_type, resource_id);
