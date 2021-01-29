package db;

import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.lang.Nullable;


public abstract class Migration implements JavaMigration {

  private final Matcher nameMatch;

  private final boolean isRepeatable;

  protected Migration() {
    final String className = getClass().getSimpleName();
    final String versionedRegex = "^(V)([0-9]*)([A-Z]\\w*)$";
    final String repeatableRegex = "^(R)([A-Z]\\w*)$";
    final Pattern versionedPattern = Pattern.compile(versionedRegex);
    final Pattern repeatablePattern = Pattern.compile(repeatableRegex);
    final Matcher versionedMatcher = versionedPattern.matcher(className);
    final Matcher repeatableMatcher = repeatablePattern.matcher(className);

    if (!versionedMatcher.matches() && !repeatableMatcher.matches()) {
      throw new FlywayException("[FATAL] Invalid migration class name");
    }

    this.isRepeatable = repeatableMatcher.matches();
    this.nameMatch = repeatableMatcher.matches()
      ? repeatableMatcher
      : versionedMatcher;
  }

  @Override
  public boolean canExecuteInTransaction() {
    return true;
  }

  @Override
  public Integer getChecksum() {
    return this.up().hashCode();
  }

  @Override
  public String getDescription() {
    return this.nameMatch.group(this.isRepeatable ? 2 : 3);
  }

  @Override
  @Nullable
  public MigrationVersion getVersion() {
    return !this.isRepeatable
      ? MigrationVersion.fromVersion(this.nameMatch.group(2))
      : null;
  }

  @Override
  public boolean isUndo() {
    return false;
  }

  @Override
  public void migrate(final Context context) throws Exception {
    try (PreparedStatement statement = context.getConnection().prepareStatement(this.up())) {
      statement.execute();
    }
  }

  public abstract String up();

  public abstract String down();
}
