#Client-cloud protein-protein docking application.#

##Overview##

This application bootstraps a self-scaling distributed computational backend in the MS Azure cloud
and exposes a RESTful API for programmatically running docking simulations\n
from a desktop molecular viewer.

The bootstrap phase is implemented in .NET C#, and the rest of the backend is implemented in Java.

The distributed workflow execution and file staging to compute nodes are provided by [CCTools WorkQueue](http://ccl.cse.nd.edu/software/)

##Documentation##

[Client user API](/andreyto/proddl-c/src/master/UserManual.txt)

[Developer's Manual](/andreyto/proddl-c/src/master/DeveloperManual.txt) for building, deploying and modifying

[Creating a x509 Certificate for the Windows Azure Management API](/andreyto/proddl-c/src/master/CreatingJavaCertificate.html), a step required for deployment as referenced by the Developer's Manual

##Authors##
Andrey Tovchigrechko `<andreyto AT gmail.com>` and [Hyunsoo Daniel Kim](https://www.linkedin.com/pub/hyunsoo-daniel-kim/14/1a5/166)

##License##
GPLv3. See also COPYING file that accompanies the source code.
