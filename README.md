Server Side:  

              Assignment 2&3:   httpfs -d D:/onedrive/COMP/COMP445/lab/A3
                                httpfs -v -d D:/onedrive/COMP/COMP445/lab/A3
                                httpfs -v -p 8080 -d D:/onedrive/COMP/COMP445/lab/A3


Client side :

             A1:httpc get -v 'http://localhost/get?course=networking&assignment=1'
                httpc post -h Content-Type:application/json --d '{"Assignment": 1}' 'http://localhost/post'


             A2:httpc post -v -h Content-Type:application/json --d '{"Assignment": 210}' 'http://localhost/inputBody.txt'
                httpc post -v -h Content-Type:application/json --d '{"Assignment": 210}' 'http://localhost/xx.txt'
                httpc get -v 'http://localhost'
                httpc get -v 'http://localhost/inputBody.txt'
                httpc get -v 'http://localhost/input.txt'

             A3:httpc post -v -h Content-Type:application/json --d '{"Assignment": 210}' 'http://localhost:8007/inputBody.txt'
                httpc post -v -h Content-Type:application/json --d '{"Assignment": 210}' 'http://localhost:8007/xx.txt'
                httpc get -v 'http://localhost:8007'
                httpc get -v 'http://localhost:8007/inputBody.txt'
                httpc get -v 'http://localhost:8007/input.txt'
                httpc get -v 'http://localhost:8007/get?course=networking&assignment=1'
                httpc post -h Content-Type:application/json --d '{"Assignment": 1}' 'http://localhost:8007/post'

Router  :       

              router --port=3000 --drop-rate=0.2 --max-delay=10ms --seed=1
