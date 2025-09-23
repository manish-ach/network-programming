use std::{
    io::{Read, Write},
    net::{TcpListener, TcpStream},
    thread,
};

fn main() -> std::io::Result<()> {
    let listener = TcpListener::bind("127.0.0.1:7878")?;
    println!("Server listening on 127.0.0.1:7878");

    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                thread::spawn(|| handle_clients(stream));
            }
            Err(e) => eprintln!("Connection failed: {}", e),
        }
    }
    Ok(())
}

fn handle_clients(mut stream: TcpStream) {
    let peer = stream.peer_addr().unwrap();
    println!("Client connected {}", peer);

    let mut buffer = [0; 512];
    loop {
        let bytes_read = match stream.read(&mut buffer) {
            Ok(0) => {
                println!("Client {} diconnected", peer);
                break;
            }
            Ok(n) => n,
            Err(e) => {
                eprintln!("Error reading from {}: {}", peer, e);
                break;
            }
        };

        if let Err(e) = stream.write_all(&buffer[..bytes_read]) {
            eprintln!("Error  writing to {}: {}", peer, e);
            break;
        }
    }
}
