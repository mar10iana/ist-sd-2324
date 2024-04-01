class ServerEntry:
    def __init__(self, address, qualifier):
        # Initialize a server entry with an address and a qualifier
        self.address = address
        self.qualifier = qualifier

class ServiceEntry:
    def __init__(self, name):
        # Initialize a service entry with a name and an empty list of servers
        self.name = name
        self.servers = []

    def add_server(self, server_entry):
        # Add a server entry to the list of servers for this service
        self.servers.append(server_entry)

class NamingServerState:
    def __init__(self):
        # Initialize the naming server with an empty dictionary of services
        self.services = {}

    def register_server(self, request):
        # Register a new service or add a server to an existing service
        name = request.name
        qualifier = request.qualifier
        address = request.address

        server_entry = ServerEntry(address, qualifier)
        service_entry = ServiceEntry(name)

        # TODO: Create a new service entry if the service name is not already registered
        if name not in self.services:
            self.services[name] = service_entry

        # Add the server entry to the server list of the service
        self.services[name].add_server(server_entry)

    def lookup_service(self, request):
        # Return a list of server addresses matching the service name and qualifier
        service_name = request.name
        qualifier = request.qualifier
        service_entry = self.services.get(service_name)

        matching_servers = []

        if service_entry:
            # Filter servers by qualifier, if provided
            for server in service_entry.servers:
                if not qualifier or server.qualifier == qualifier:
                    matching_servers.append(server.address)

        return matching_servers
    
    def delete_service(self, request):
        # Delete a server from a service or the entire service if no servers remain
        name = request.name
        address = request.address

        if name in self.services:
            # Remove the server with the matching address
            self.services[name].servers = [server for server in self.services[name].servers if server.address != address]

            # If no servers remain for this service, delete the service entry
            if not self.services[name].servers:
                del self.services[name]
