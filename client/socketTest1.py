import asyncio
import websockets
import time

async def hello(uri, header, timeout):
    async with websockets.connect(uri, extra_headers=header) as ws:
        flag = ""
        print("pas")
        
        # guest name
        flag = await ws.recv()
        print(f"< {flag}")

        # receive guest ready signal
        flag = await ws.recv()
        print(f"< {flag}")
        time.sleep(2)

        # send start
        await ws.send(str(-1))
        # receive start signal
        flag = await ws.recv()
        print(f"< {flag}")

        await ws.send(str(256))
        flag = await ws.recv()
        print(f"< {flag}")
        flag = await ws.recv()
        print(f"< {flag}")

        await ws.send(str(1281))
        flag = await ws.recv()
        print(f"< {flag}")
        flag = await ws.recv()
        print(f"< {flag}")

        await ws.send(str(2306))
        flag = await ws.recv()
        print(f"< {flag}")
        flag = await ws.recv()
        print(f"< {flag}")

        await ws.send(str(3331))
        flag = await ws.recv()
        print(f"< {flag}")
        flag = await ws.recv()
        print(f"< {flag}")

        await ws.send(str(4356))
        flag = await ws.recv()
        print(f"< {flag}")
        
        
        time.sleep(timeout)
        ws.close()

asyncio.get_event_loop().run_until_complete(hello('ws://localhost:8080/playing', {"role": "m", "roomName":"123#456", "userName":"123", "masterStone":"1"}, 60))
