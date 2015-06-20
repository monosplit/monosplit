# MonoSplit
MonoSplit application

This application helps you to convert your monolithic application into micro services.

## Why use MonoSplit?

Because you started your project monolithic. What you've done is not incorrect and your point is very well supported by people like Martin Fowler as described here:
http://www.martinfowler.com/bliki/MonolithFirst.html

Now you might want to experiment with micro services, or completely migrate into micro service architecture. This is why MonoSplit was developed. It helps growing monolithic applications to be split into micro services automatically. Furthermore it supports load balancers (as of the moment only HAProxy) so with one run, your codebase is split, your split micro services are automatically deployed and your load balancer is automatically configured.


## Known issues:
 -No file based databases that are only available within the project itself. As your codebase is split, the whole project will be copied into another directory. So any project directory based file changes will not be detected/used by the other services. In short, make sure your existing database changes can be done anywhere from the system without changing any configuration (such as databases accesible over IP or full path of the database file)
