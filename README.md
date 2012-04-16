# Dozer Wicket Hibernate model

The Dozer Wicket Hibernate model is a Wicket IModel implementation to wrap a Hibernate object and keeping its changed values for several requests (instead of a simple LDM which re-loads a object from the database when re-attaching). To avoid serializing Hibernate proxies the model detaches any unintialized Hibernate proxies (they are replaced with `null` values and marked a attachable in the model instance) that are re-attached when invoking `getObject` again, already initialized Hibernate proxies are deproxied.

The model also supports non-Hibernate objects, when detaching such an object it will check it for any references to Hibernate objects and detach them.

Multiple Hibernate factories for different databased are also supported, the `SessionFinder` interface is given the current object class as type. Based on this type the correct Hibernate session can be returned by the user.

## Details

In the `onDetach` of the model the object tree is traversed, when a Hibernate proxy is encountered its checked if it is initialized, if so the object is deproxied. When an object is unintialized a hint is registered in the model and the original value is reset to `null`. While re-attaching the model (first invoke of `getObject` in a new request) al the registered hints are reattached as Hibernate proxies. The proxies are created using Hibernate's internal API, the same way Hibernate does while loading objects and or collections.

## Usage

### Prerequisites

* Java 7
* Wicket 1.5.x
* Hibernate 4.1.x (only 4.1.x supported since it uses internal Hibernate API's for re-attaching, for 3.6.x see other branch)
* Spring (a Spring bean implementing the `SessionFinder` interface to get access to the Hibernate session, injected using `wicket-spring`)

### Creation:

* `DozerModel` for a normal model
* `DozerListModel` list model version (maintains a list of `DozerModel` objects)


### Don'ts:

* Don't use lazy initialization in getters (for a `List` for example), it can cause the following behavior:
    1 Object is set in the model, contains a `List` member
	2 On detach the `List` is still a proxy, and detached to `null`
	3 On attach the model gets the current value of the `List` member, because it was set to `null` lazy initialization will return a new empty list, the model will see this 'new' `List` as a changed value and not re-attach the original `List` as a proxy, effectively clearing the values...
* Watch out for Hibernate's auto-flush behavior (if enabled for the current session), because the model changes the original loaded Hibernate objects it can cause a flush of the model in (half) detached state, causing values to be saved as `null` to the database...