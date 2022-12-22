# excel-comparison-tool

For whole project - simple build mvn clean package

For Desktop application:

Since JFX is fully modularized, all dependencies should be moduled otherwise the build will fail.
Libraries that are not modularized in Maven central repositories were modularized and added to the libs folder.
In order to use them during build time, first we have to run next command:
mvn validate

After that, we can build JFX image:
mvn clean compile javafx:jlink

In order to create executable install file, next command has to be executed:
jpackage --type msi --app-version 1.0 --description "ExcelComparisonTool is a desktop tool for generating comparison report between two Excel spreadsheet." --name ExcelComparisonTool --dest d:\work\ExcelComparatorTool --vendor Riezvan_Naumenko --runtime-image d:\excel-comparison-tool\desktop-excel-comparison-tool\target\ExcelComparisonTool

Where:
--type - type of created installation file. It can be exe or msi or pkg. You can create installation file only applicable for your type of OS.
--app-version - version of tool
--description - short description
--name - name of created tool
--dest - path to folder to store installation file
--vendor - short name of vendor
--runtime-image - path to the built JFX image. 

P.S. How to create a modularized jar:
1. Generate module-info.java. moduled is a folder 
jdeps --generate-module-info moduled commons-io-2.11.0.jar
In case it can not generate module-java.file, please add "--ignore-missing-deps" option.
2. Extract files from jar to current folder
jar xf commons-io-2.11.0.jar
3. Compile module-info
javac -p converted -d extracted_jar moduled\org.apache.commons.io\module-info.java
4. Update jar with compiled module-info.class file.
jar --update --file commons-io-2.11.0.jar -C extracted_jar module-info.class
5. Add modularized lib into required maven project folder
6. Add next plugin into build -> plugins section like this:
   `<plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-install-plugin</artifactId>
       <version>2.5.1</version>
       <configuration>
           <groupId>commons-io</groupId>
           <artifactId>commons-io</artifactId>
           <version>2.11.0</version>
           <packaging>jar</packaging>
           <file>${basedir}/libs/commons-io-2.11.0.jar</file>
           <generatePom>true</generatePom>
       </configuration>
       <executions>
           <execution>
               <id>install-jar-lib</id>
                   <goals>
                       <goal>install-file</goal>
                   </goals>
                   <phase>validate</phase>
           </execution>
       </executions>
   </plugin>`