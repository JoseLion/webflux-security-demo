package db.migration;

import db.Migration;

public class V1CreateAccountTable extends Migration {

  @Override
  public String up() {
    return """
      CREATE TABLE account (
        id SERIAL NOT NULL PRIMARY KEY,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        username TEXT NOT NULL,
        password TEXT,
        is_locked BOOLEAN NOT NULL,
        is_expired BOOLEAN NOT NULL,
        UNIQUE (username)
      );
    """;
  }

  @Override
  public String down() {
    return "DROP TABLE account;";
  }
}
