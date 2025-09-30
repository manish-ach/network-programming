use std::{
    fs,
    io::{Read, Write},
    net::{TcpListener, TcpStream},
};

fn handle_client(mut stream: TcpStream) {
    let mut buffer = [0; 512];
    if let Ok(_) = stream.read(&mut buffer) {
        let html = fs::read_to_string("public/index.html")
            .unwrap_or_else(|_| "<h1>File not Found :P</h1>".to_string());

        let response = format!(
            "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset\r\n\r\n{}",
            html
        );

        stream.write_all(response.as_bytes()).unwrap();
    }
}

fn main() -> std::io::Result<()> {
    let listener = TcpListener::bind("127.0.0.1:8001")?;
    println!("Server listening on http://127.0.0.1:8001");

    for stream in listener.incoming() {
        if let Ok(stream) = stream {
            std::thread::spawn(|| handle_client(stream));
        }
    }

    Ok(())
}
