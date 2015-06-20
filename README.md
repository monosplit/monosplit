# MonoSplit
MonoSplit application

This application helps you to convert your monolithic application into micro services.

## Why use MonoSplit?

Because you started your project monolithic. What you've done is not incorrect and your point is very well supported by people like Martin Fowler as described here:
http://www.martinfowler.com/bliki/MonolithFirst.html

Now you might want to experiment with micro services, or completely migrate into micro service architecture. This is why MonoSplit was developed. It helps growing monolithic applications to be split into micro services automatically. Furthermore it supports load balancers (as of the moment only HAProxy) so with one run, your codebase is split, your split micro services are automatically deployed and your load balancer is automatically configured.

## Demo
In order to test the demo application, use https://github.com/monosplit/Rails-4-Twitter-Clone project, make sure you have everything to run Rails applications, also don't forget to run `bundle install` and `rake db:migrate` commands on the rails application. For the given Twitter clone application, MonoSplit allows up to 8 microservices to be created!

## Usage
Binaries are not available, so clone the project and install it with mvn install. Then copy the monosplit-<version>-jar-with-dependencies.jar file whichever directory you would like to. Then copy `command.sh`, `haproxy.cfg` and `proxydeploy.sh` files into the same directory. You can keep `config.yaml` file wherever you want to but you will need to specify the location of it as a parameter to MonoSplit. After configuring these files run the jar file with `config.yaml` file location such as `java -jar monosplit-1.0-SNAPSHOT-jar-with-dependencies.jar ../config.yaml`. The below are the explanations of these configuration files:
### command.sh
This is the shell script where you need to configure deployment of micro services. `${monosplit.ip}` and `${monosplit.port}` variables should be used for the microservice's ip address and port number. By default it is configured to run rails server on the local machine. Note that, this script cannot be blocking, it must run on the background.
### config.yaml
This is the file where you specify the settings for MonoSplit to run. The below are the explanations of variables:
#####  projectPath
This variable is the full path of the monolithic application 
#####  copyFolderPrefix
This variable is the prefix of the new microservices' path. By convention a number will be added at the end of this prefix, ie if it is ../app, then first microservice will be located in ../app1.
#####  ipAddress
This variable is the IP address of microservices. Right now MonoSplit only supports one IP address.
#####  basePortNumber
This variable is the base port number of microservices. It will have the same numbering convention as the `copyFolderPrefix`, ie if it is set as 3000, then first microservice's port number will be 3001.
#####  microServiceAmount
This variable is the total amount of microservices to be created. On top of this, there will be a default remaining services created, which will contain rest of the endpoints, and by convention will take the base port number and 0 number after the file name prefix. Note that, if you set this number higher than the available endpoints on your application, then you will get `IndexOutOfBoundsException` error which will tell the maximum amount of endpoints available such as `java.lang.IndexOutOfBoundsException: Index: 8, Size: 8` would mean that `microServiceAmount` variable can be maximum 8.
### haproxy.cfg
This is the configuration file for HAProxy, configure everything you want in your way, make sure you put your front end configuration and then mark the beginning and end of the autogenerated services by adding `# ${monosplit.beginconfig}` and `# ${monosplit.endconfig}` respectively. At the end of the program run, MonoSplit will generate `generatedproxy.cfg` file, which will contain the final result before load balancer deployment.
### proxydeploy.sh
This is the shell script for deploying the HAProxy settings, always make sure that you are copying `generatedproxy.cfg` file to the HAProxy's configuration directory and then restart HAProxy.

## How does it work?

MonoSplit takes your monolithic web appliccation, then checks it's routing table, then starts splitting into micro services by the highest amount of endpoints available. The below is the demo's routing table:
```
        Prefix Verb   URI Pattern                    Controller#Action
          root GET    /                              static_pages#home
following_user GET    /users/:id/following(.:format) users#following
followers_user GET    /users/:id/followers(.:format) users#followers
         users GET    /users(.:format)               users#index
               POST   /users(.:format)               users#create
      new_user GET    /users/new(.:format)           users#new
     edit_user GET    /users/:id/edit(.:format)      users#edit
          user GET    /users/:id(.:format)           users#show
               PATCH  /users/:id(.:format)           users#update
               PUT    /users/:id(.:format)           users#update
               DELETE /users/:id(.:format)           users#destroy
      sessions POST   /sessions(.:format)            sessions#create
   new_session GET    /sessions/new(.:format)        sessions#new
       session DELETE /sessions/:id(.:format)        sessions#destroy
        tweets GET    /tweets(.:format)              tweets#index
               POST   /tweets(.:format)              tweets#create
         tweet DELETE /tweets/:id(.:format)          tweets#destroy
 relationships POST   /relationships(.:format)       relationships#create
  relationship DELETE /relationships/:id(.:format)   relationships#destroy
        signup GET    /signup(.:format)              users#new
        signin GET    /signin(.:format)              sessions#new
       signout DELETE /signout(.:format)             sessions#destroy
         about GET    /about(.:format)               static_pages#about
                      /*path(.:format)               application#routing_error
```

In the example above, first microservice candidate would be the `/users` endpoint, because this endpoint has the most entries, then it will go by `/sessions` and so on. Each of the created micro services only contain their corresponding controller.

After these microservices created, their deployment script will be run by MonoSplit. Once all microservices are deployed, HAProxy configuration will be set and deployed as well.

Currently MonoSplit only supports Ruby on Rails framework, more language/framework support will be added.


## Known issues:
 -No file based databases that are only available within the project itself. As your codebase is split, the whole project will be copied into another directory. So any project directory based file changes will not be detected/used by the other services. In short, make sure your existing database changes can be done anywhere from the system without changing any configuration (such as databases accesible over IP or full path of the database file)
