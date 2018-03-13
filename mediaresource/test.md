# A Standard Java Container-based Development and Continuous Integration Pipeline

## What it is
A docker-compose based infrastructure containing gitlab,jenkins,sonarqube,maven and nexus

## What it is not
completed, ready to use


# Guide

This is how to set your server up

##create data directories:
```{r, engine='bash', count_lines}
cat docker-compose.yml  | grep 'CI_HOME}/data' | sed -e 's/.*\}\/\(data.*\)\:.*/\1/' | xargs -n 1 -I % mkdir -p %
cat docker-compose.yml  | grep 'CI_HOME}/data' | sed -e 's/.*\}\/\(data.*\)\:.*/\1/' | xargs -n 1 -I % touch %/.keepme
cat docker-compose.yml  | grep 'CI_HOME}/data' | sed -e 's/.*\}\/\(data.*\)\:.*/\1/' | xargs -n 1 -I % cchmod -R o+w %
chown 1000:1000 data/jenkins/data
chown 1000:1000 data/nexus/data
chmod u+rw data/jenkins/data
chmod u+rw data/nexus/data
```


##Set up loadalanacer
Decide whether to use ssl, if yes, follow comments in services/loadbalancer/Dockerfile

```{r, engine='bash', count_lines}
cd services/loadbalancer
docker build . -t stdcdevenv/loadbalancer
cd ../..
```

## Create .env
```{r, engine='bash', count_lines}
vi .env
```

Content

```{r, engine='bash', count_lines}
CI_HOME=/opt/projects/java-ci-docker
PASSWORD_SONAR=stdcdevenv
URL_EXTERNAL=http://dockervm
```

URL_EXTERNAL is your externally visible hostname, modify to https if set up
In this example the external name is "dockervm"

##Start servers
```{r, engine='bash', count_lines}
cd services/loadbalancer ; docker build
docker-compose up -d
```

Gitlab takes a while ...


## Gitlab
Go to 

```{r, engine='bash', count_lines}
http://dockervm/gitlab
```

and set up user(s)


### gitlab ssh access

```{r, engine='bash', count_lines}
ssh-keygen -t rsa -b 4096
```

```{r, engine='bash', count_lines}
vi ~/.ssh/config
```

Content 

```{r, engine='bash', count_lines}
Host dockervm
  Hostname dockervm
  User git
  Port 10022
```

### Set up gitlab
gitlab: root/manager01
        default-user/default01
		
Create project "default-project" for default-user

### push code to git

Clone, commit and push to 
```{r, engine='bash', count_lines}
git@dockervm:default-user/default-project.git
```

or git add remote and push

```{r, engine='bash', count_lines}
git remote add dockervm git@dockervm:default-user/default-project.git
git push dockervm master
```

##Jenkins

```{r, engine='bash', count_lines}
http://dockervm/jenkins/
```

Get the initial Secret

```{r, engine='bash', count_lines}
docker exec -ti jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Setup account root/manager01

Add your public ssh key in jenkins to gitlab default-user

```{r, engine='bash', count_lines}
docker exec -ti jenkins ssh-keygen -t rsa -b 4096
docker exec -ti jenkins cat /var/jenkins_home/.ssh/id_rsa.pub
```

Setup nexus for jenkins

```{r, engine='bash', count_lines}
vi data/jenkins/data/.m2/settings.xml
```

Content

```xml
<settings>

  <mirrors>
    <mirror>
      <id>internal-repository</id>
      <name>Maven Repository Manager running on repo.mycompany.com</name>
      <url>http://dockervm/nexus/repository/maven-public/</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
    <profiles>
        <profile>
            <id>downloadSources</id>
            <properties>
                <downloadSources>true</downloadSources>
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>downloadSources</activeProfile>
    </activeProfiles>

    <servers>
       <server>
          <id>nexus-snapshots</id>
          <username>deploy-snapshot</username>
          <password>snapshot01</password>
       </server>
       <server>
          <id>nexus-releases</id>
          <username>deploy-release</username>
          <password>release01</password>
       </server>
    </servers>
	
</settings>

```

# Nexus

```{r, engine='bash', count_lines}
http://dockervm/nexus/
```

Account admin/manager01

Manage users and roles

```{r, engine='bash', count_lines}
		
    User: deploy-snapshot/snapshot01
		role: deploy-snapshot
			permissions
				nx-repository-view-maven2-maven-snapshots-add
				nx-repository-view-maven2-maven-snapshots-edit
	
    User: deploy-release/release01
		role: deploy-release
			permissions
				nx-repository-view-maven2-maven-release-add
				nx-repository-view-maven2-maven-release-edit

```
	
# Sonarqube

```{r, engine='bash', count_lines}
http://dockervm/sonarqube/
```

Change user to admin/manager01

Generate a token for your account and copy it ...

e.g.

```{r, engine='bash', count_lines}
02bd5171bec65b475159ac2a9176c16ca48ac714
```

Check for qualitiy profiles, if emtpy got to Administration-Marketplace and install required plugins, e.g. Sonar-Java,PMD, Checkstyle, ...

# Jenkis Build

Create project default-project

Create a simple build job

SCM is

```{r, engine='bash', count_lines}
git@gitlab:default-user/default-project.git
```

Maven goals

```{r, engine='bash', count_lines}
install sonar:sonar deploy
```

Properties are

```{r, engine='bash', count_lines}
sonar.host.url=http://sonarqube:9000/sonarqube
sonar.login=02bd5171bec65b475159ac2a9176c16ca48ac714
```

Build Project - you should see results in Nexus, Sonarqube and of course Jenkins!


