# User Manual #

## REST API - curl ##

*Service address (and port number if available) refernced below should be provided 
by the whoever has deployed the API server*

### JOB ###
        
- getting job list
        
```
            $ curl {address}/pdl/r/joblist -u {username}:{password}
            {
                "job":
                [
                    "{jobType}:{jobId}:{status}",
                    "cert:2c00d72c-af53-bb3e-f302-97a544e90fdb:finished",
                    "execute:08070f60-447e-dad1-55cd-ba3f001f9cb4:failed"
                ]
            }
```

- retrieving job information
        
```
            $ curl {address}/pdl/r/job?jid={jobId} -u {username}:{password}
            {
                "info":
                {
                    "input":"{\"job\":\"execute\",\"some\":\"test\",\"user\":\"admin\"}",
                    "name":"execute",
                    "status":"failed",
                    "user":"admin"
                }
            }
```
        
- getting job result file id 
        
```
            $ curl {address}/pdl/r/job/result?jid={jobId} -u {username}:{password}
```
            
- submitting a job
        
```
            $ curl {address}/pdl/r/job/[jobType] -u {username}:{password} -d '{"interpreter":"[interpreter]", "script":"{scriptFileId}", "input":"{inputFileId}"}' -H "Content-Type: application/json" -X POST
                [jobType] -
                    execute : executes a job with an interpreter given by user
                    cert : creates certificates for Java main application (Admin)
                    addUser : adds new user (Admin)
                [interpreter]
                    makeflow : cctools makeflow
                    bash : cygwin bash (Admin)
                    exe : executable binary (Admin)
```

*data input fields can be omitted as necessary*
    
### FILE ###

- uploading a file
        
```
            $ curl {address}/pdl/r/file/upload -F file={filePath} -X POST -u {username}:{password}
            {
                "id":"06ab5131-67ea-6678-a058-eb1b8be15b2c"
            }
```
            
- downloading a file
        
```
            $ curl {address}/pdl/r/file/get?id={fileId} -o {filePath on local machine} -u {username}:{password}
``` 
            
- creating a new file (without physical file uploaded)
        
```
            $ curl {address}/pdl/r/file/new -u {username}:{password}
            {
                "id":"91a59de4-809c-4f7d-2e74-9c0711374b66",
                "path":"410/91a59de4-809c-4f7d-2e74-9c0711374b66.dat"
            }
```
            
- committing a file (to update file status)
        
```
            $ curl {address}/pdl/r/file/commit?id={fileId} -u {username}:{password}
            {
                "result":"file committed"
            }
```
        
- deleting a file
        
```
            $ curl {address}/pdl/r/file/delete?id={fileId} -u {username}:{password}
            {
                "result":"file deleted"
            }
```
        
- getting list of user files
        
```
            $ curl {address}/pdl/r/filelist -u {username}:{password}
            {
                "file":
                [
                    "{fileId}:{originalFileName}:{status}",
                    "04a3d59c-216c-3fb8-05d3-755acf621fb8:file1.zip:committed",
                    "0685972c-91bd-163b-e95c-fdaa72d60e8d:file2.txt:committed"
                ]
            }
```
    
### SCALE UP/DOWN ###
    
- getting current nnumber of instances
        
```
            $
            {
                "max":96, //max number of worker instances allowed
                "c_c":1 //current count of worker nodes, '0' means request failed
            }
```    
        
- changing number of worker instances (scale cloud instance)
        
```
            $ curl {address}/pdl/r/job/scale -d '{"n_worker":"{numberOfInstance}"}' -H "Content-Type: application/json" -X POST -u {username}:{password}
            {
                "id":"749c72d2-174e-f67c-8193-a1c9225b6dce",
                "name":"scale",
                "result":"submitted"
            }
```

## How to use python integration testing harness ##

```
    python {path to proddl.py} {service address} {# of iterations} path {path to timer.py} {input file path}
```
    
*example*: ```python /test/proddl.py 127.0.0.1 5 path /test/timer.py /test/test.input```
