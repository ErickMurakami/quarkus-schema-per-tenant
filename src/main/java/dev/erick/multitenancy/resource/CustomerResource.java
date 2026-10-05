package dev.erick.multitenancy.resource;

import dev.erick.multitenancy.dto.CreateCustomerRequest;
import dev.erick.multitenancy.dto.CustomerResponse;
import dev.erick.multitenancy.model.Customer;
import dev.erick.multitenancy.service.CustomerService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/customers")
@RolesAllowed("user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService service;

    @GET
    public List<CustomerResponse> list(){
        return service.findAll().stream().map(CustomerResponse::from).toList();
    }

    @POST
    public Response create(CreateCustomerRequest request){
        Customer customer = service.save(request.name());
        return Response.created(URI.create("/customers/" + customer.getId())).entity(CustomerResponse.from(customer)).build();
    }
}
