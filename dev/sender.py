import logging
import socket
from time import sleep
import socket

from zeroconf import IPVersion, ServiceInfo, Zeroconf

# The name should be something like Vidify + system specs.
# This part should also check that there aren't services with the same name
# already.
SERVICE_NAME = "vidify - Test Service"
# The service type includes the protocols used, like this:
# "_<protocol>._<transportlayer>".
# For now, the data is transmitted with TCP, so this is enough.
SERVICE_TYPE = "_vidify._tcp.local."
# Obtaining the local address where the server will run
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.connect(('8.8.8.8', 1))  # connect() for UDP doesn't send packets
ADDRESS = s.getsockname()[0]
print("USING ADDRESS", ADDRESS)
# The port shouldn't be hardcoded. Perhaps setting it to zero already handles
# the automatic initialization.
PORT = 30010

logging.basicConfig(level=logging.DEBUG)
logging.getLogger('zeroconf').setLevel(logging.DEBUG)
ip_version = IPVersion.All

desc = {'path': '/'}

info = ServiceInfo(
    SERVICE_TYPE,
    f"{SERVICE_NAME}.{SERVICE_TYPE}",
    addresses=[socket.inet_aton(ADDRESS)],
    port=PORT,
    properties=desc,
    server="ash-2.local.",
)

zeroconf = Zeroconf(ip_version=ip_version)
print("Registration of a service, press Ctrl-C to exit...")
zeroconf.register_service(info)
try:
    while True:
        sleep(0.1)
except KeyboardInterrupt:
    pass
finally:
    print("Unregistering...")
    zeroconf.unregister_service(info)
    zeroconf.close()
