# Configure Maven to use your Knox Admin password

First time only! Follow this guide for explanations: http://maven.apache.org/guides/mini/guide-encryption.html

Generate an (obfuscated) master password and store it in `~/.m2/settings-security.xml`
```
$ mvn --encrypt-master-password
...
```

`settings-security.xml` will look like
```
<settingsSecurity>
  <master>{your_encoded_master_password}</master>
</settingsSecurity>
```

Encrypt your Knox admin user's password (default: admin-password)
```
$ mvn --encrypt-password
Password: admin-password
```

Create a `servers` element in your `~/.m2/settings.xml` which stores your Knox username and password
```
<settings ...>
  <servers>
    <server>
      <id>local-knox</id>
      <username>admin</username>
      <password>{encrypted_admin_password}</password>
    </server>
  </servers>
</settings>
```

Finally, ensure that this exemplar has the correct URL for your Knox gateway. Be sure to use the same `id` in
`settings.xml` as well as this local `pom.xml`.
```
<project>
  ...
  <repositories>
    <repository>
      <id>local-knox</id>
      <url>https://localhost:8443/gateway/sandbox/hbase/maven</url>
      <name>HBase Jars via Knox</name>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
</project>
```

# Set up trust

Copy JVM CACERTS
```
$ cp $JAVA_HOME/jre/lib/security/cacerts truststore.jks
```

Add in the Knox certificate
```
$ keytool -import-keystore -srckeystore $KNOX_HOME/data/security/keystores/gateway.jks -destkeystore truststore.jks
```

# Build the exemplar

Build the examples:
```
$ mvn -Djavax.net.ssl.trustStore=truststore.jks clean package
```

# Run the exemplar
Run the example:

```
$ mvn exec:exec -Dexec.executable=java -Dexec.args="-cp target/nosql-libs/*:target/knox-hbasejars-exemplar-0.0.1-SNAPSHOT.jar:$HBASE_CONF_DIR com.github.joshelser.hbase.Client"
```
