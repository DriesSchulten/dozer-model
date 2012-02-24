# Dozer Wicket Hibernate model

The Dozer Wicket Hibernate model is a Wicket IModel implementation to wrap a Hibernate object and keeping its changed values for serveral requests (instead of a simple LDM which re-loads a object from the database when re-attaching). To avoid serializing Hibernate proxies the model detaches any unintialized Hibernate proxies that are re-attached when invoking `getObject` again.

## Details

In the `onDetach` of the model the object tree is traversed, when a Hibernate proxy is encounterd its checked if it is initialized, if so the object is unenhanced to remove the proxy. When an object is unintialized a hint is registerd in the model and the original value is reset to `null`. While re-attaching the model (first invoke of `getObject` in a new request) al the registerd hints are reattached as Hibernate proxies. The proxies are created using Hibernate's internal API, the same way Hibernate does while loading objects and or collections.

## Usage

### Prerequisites

* Java 7
* Wicket 1.5.x
* Hibernate 4.1.x (only 4.1.x supported since it uses internal Hibernate API's for re-attaching, for 3.6.x see other branch)
* Spring (a Spring bean implementing the `SessionFinder` interface to get access to the Hibernate session, injected using `wicket-spring`)

### Creation:

* `DozerModel` for a normal model
* `DozerListModel` list model verison (maintains a list of `DozerModel` objects)