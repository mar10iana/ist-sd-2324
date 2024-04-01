import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from NamingServerState import ServerEntry, ServiceEntry, NamingServerState

class NameServerServiceImpl(pb2_grpc.NameServerServiceServicer):

    def __init__(self):
        self.naming_server = NamingServerState()

    def register(self, request, context):
        
        # Register the service in the NamingServer
        self.naming_server.register_server(request)

        # Return an empty RegisterResponse (since no arguments are defined for it)
        return pb2.RegisterResponse()
    
    def lookup(self, request, context):

        # Perform the lookup in the NamingServer
        server_addresses = self.naming_server.lookup_service(request)

        # Construct and return the response
        response = pb2.LookupResponse()
        response.servers.extend(server_addresses)
        
        return response
    
    def delete(self, request, context):

        # Delete the service from the NamingServer
        self.naming_server.delete_service(request)

        # Return an empty DeleteResponse (since no arguments are defined for it)
        return pb2.DeleteResponse()