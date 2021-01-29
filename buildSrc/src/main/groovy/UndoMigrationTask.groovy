import javax.inject.Inject
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

class UndoMigrationTask extends JavaExec {

  @Inject
  public UndoMigrationTask(String profile) {
    super()
    super.dependsOn('classes')

    super.setClasspath(project.sourceSets.main.runtimeClasspath)
    super.setMain('db.UndoMigration')

    if (!profile.empty) {
      super.setArgs(["--spring.profiles.active=${profile}"])
    }
  }
}
