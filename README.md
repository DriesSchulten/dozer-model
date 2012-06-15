# Dozer Wicket Hibernate model

The Dozer Wicket Hibernate model is a Wicket IModel implementation to wrap a Hibernate object and keeping its changed values for several requests (instead of a simple LDM which re-loads a object from the database when re-attaching). To avoid serializing Hibernate proxies the model detaches any unintialized Hibernate proxies (they are replaced with custom `HibernateProxy` instances) that are re-attached when invoking a method on the proxy again, already initialized Hibernate proxies are deproxied.

The model also supports non-Hibernate objects, when detaching such an object it will check it for any references to Hibernate objects and detach them.

Multiple Hibernate factories for different databased are also supported, the `SessionFinder` interface is given the current object class as type. Based on this type the correct Hibernate session can be returned by the user.

## Details

In the `onDetach` of the model the object tree is traversed, when a Hibernate proxy is encountered its checked if it is initialized, if so the object is deproxied. When an object is unintialized a Javassit proxy is generated and put in place of the original value. When the proxy gets invoked its original value is attached using internal Hibernate API's and our 'own' proxy is replaced by the Hibernate proxy.

## Usage

### Prerequisites

* Java 7
* Wicket 1.5.x
* Hibernate 3.6.x (only 3.6.x supported since it uses internal Hibernate API's for re-attaching)
* Spring (a Spring bean implementing the `SessionFinder` interface to get access to the Hibernate session, injected using `wicket-spring`)

### Creation:

* `DozerModel` for a normal model
* `DozerListModel` list model version (maintains a list of `DozerModel` objects)

## Maven repo

* Snapshot: [http://repository.topicuszorg.nl/external-snapshot/](http://repository.topicuszorg.nl/external-snapshot/)
* Release: [http://repository.topicuszorg.nl/external-releases/](http://repository.topicuszorg.nl/external-releases/)
