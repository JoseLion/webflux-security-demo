import javax.inject.Inject
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.gradle.api.Task
import org.yaml.snakeyaml.Yaml

class MigrationTask extends FlywayMigrateTask {

  @Inject
  public MigrationTask(String profile) {
    super();
    super.dependsOn('classes')

    String suffix = !profile.empty ? "-${profile}" : ""
    File propsFile = new File("${project.sourceSets.main.resources.srcDirs[0]}/application${suffix}.yml")

    if (propsFile.exists()) {
      def props = new Yaml().load(propsFile.newInputStream())

      url = props.spring.r2dbc.url.replace('r2dbc', 'jdbc')
      user = props.spring.r2dbc.username
      password = props.spring.r2dbc.password
      locations = ['classpath:db/migration']
    }
  }
}
