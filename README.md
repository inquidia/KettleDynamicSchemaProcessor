Inquidia Kettle Dynamic Schema Processor
============================

The Kettle Dynamic Schema Processor from Inquidia Consulting allows you to implement a Dynamic Schema Processor in PDI abstracting you from the Java code and allowing anyone to write a dynamic schema processor.  http://inquidia.com/news-and-info/custom-analytics-simplified-visual-design-dynamic-pentaho-mondrian-schemas

Installation:
--------------------------

1. Place the KettleDynamicSchemaProcessor jar in the `<BISERVER_HOME>/tomcat/webapps/pentaho/WEB-INF/lib` directory
2. If you plan to store the Kettle transformation in the Jackrabbit repository place the libpensol jar in the `<BISERVER_HOME>/tomcat/webapps/pentaho/WEB-INF/lib` directory.  The libpensol jar may be downloaded from: http://repo.pentaho.org/artifactory/repo/pentaho-library/libpensol/JCR-SNAPSHOT/
3. Some useful CDE dashboards to help with development may be uploaded to BA server from the KettleDSPUtils directory.

Usage:
---------------------------

1. Add the following properties to your Mondrian connection properties:
  1. DynamicSchemaProcessor: org.inquidia.mondrian.dynamicschemaprocessor.KettleDynamicSchemaProcessor
  2. kettleTransFile: The path to the transformation to use as the DSP.
    1. If the transformation is in the BA Server repository preface the path with jcr-solution://  For example jcr-solution://home/admin/pass-through.ktr
  3. (Optional) kettleStepName: (Default: OUTPUT) The name of the step in the Kettle transformation to read the output schema from.
  4. (Optional)( schemaFieldName: (Default: schema) The name of the field in the Kettle step that contains the schema.
  5. (Optional) cacheEnabled: (true/false) (Default true).  Caches the output schema for each user.  Also caches the kettle transformation to run.
  6. (Optional) KettleParameterHandler: The class to override the default parameter handler determining how parameters are passed into Kettle.

#### Kettle Parameters:

When using the default parameter handler the following parameters are passed to the transformation if and only if they are configured in the transformation:

- InputSchemaText - The schema XML associated with the Mondrian connection.
- SessionName - The username
- SessionId - The Session Id.
- SessionLocal - The user's locale
- Security.isAuthenticated - Is the user authenticated.
- Security.Role.<Role Name> - true if the user has the role.  (This is only set if the user has the role, the transformation should have a default value of false for this parameter.)
- Security.Authorities - A comma delimited list of the roles associated with the user.
- Security.User.isAccountNonExpired - Is the users account not expired.
- Security.User.isAccountNonLocked - Is the users account not locked.
- Security.User.isCredentialsNonExpired - Is the users password not expired.
- Security.isEnabled - Is the user enabled.
- Any property on the Mondrian connection is passed using the property name.

Any session attributes, or user settings will also be passed depending on what is defined in the session.  The sessionAttributesDashboard in the KettleDynamicSchemaProcessorUtils.zip will give you a full list of parameters that will be passed and there values for your session.  Be careful with these as not all of these are set for every session.

#### Clearing the Cache:

The dspCacheClearer dashboard will clear everything out of the dynamic schema processor cache.  This dashboard is found in the KettleDSPUtils folder and must be deployed in BA server.

#### Custom Parameter Handling

You may override the default parameter handling to pass things any way you want.  To do this create a Java class that implement the org.inquidia.mondrian.dynamicschemaprocessor.IParameterHandler interface.

```
public interface IParameterHandler {
   public void getParameters( Util.PropertyList connectInfo, IPentahoSession session, TransMeta transMeta );
}
```

Then specify this class in the connection property KettleParameterHandler.

#### Logging

This uses log4j logging.  By default it is configured to use ERROR level logging.

To enable logging at a lower level and write this to a separate inquidia.log file add the following to your <BISERVER_HOME>/tomcat/webapps/pentaho/WEB-INF/classes/log4j.xml file.

```
<!-- A time/date based rolling appender -->
   <appender name="INQUIDIAFILE" class="org.apache.log4j.DailyRollingFileAppender">

      <param name="File" value="../logs/inquidia.log"/>
                  <param name="Append" value="false"/>

      <!-- Rollover at midnight each day -->
      <param name="DatePattern" value="'.'yyyy-MM-dd"/>

      <!-- Rollover at the top of each hour
                            <param name="DatePattern" value="'.'yyyy-MM-dd-HH"/>
                                                   -->

      <layout class="org.apache.log4j.PatternLayout">
                     <!-- The default pattern: Date Priority [Category] Message\n -->
         <param name="ConversionPattern" value="%d %-5p [%c] %m%n"/>

         <!-- The full pattern: Date MS Priority [Category] (Thread:NDC) Message\n
                                     <param name="ConversionPattern" value="%d %-5r %-5p [%c] (%t:%x) %m%n"/>
                                                                      -->
      </layout>
   </appender>

   <category name="org.inquidia" addtivity="false">
     <priority value="ERROR"/>
     <appender-ref ref="INQUIDIAFILE"/>
   </category>
```

Log level DEBUG is very useful when testing transformations.  DEBUG level prints every parameter value it tries to pass to the Kettle transformation.  It also prints the XML schema that was output.

Building from Source
--------------------

The Kettle Dynamic Schema processor is built using Maven.  The KettleDynamicSchemaProcessor jar may be built using the `mvn clean package` command.
