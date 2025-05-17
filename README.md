# Introduction
Jansansad is a command-line tool built in Scala to download, generate data, process, and analyze Indian parliamentary speeches for past 5 Lok Sabha Sessions. It allows you to extract word-level insights across Lok Sabha sessions, making large-scale legislative discourse analyzable and visual. The project uses scalaj-http, jsoup, PDFBox, Apache Spark, EvilPlot and PostgreSQL.  

Tech Blog - https://medium.com/@siddharthbanga/what-are-they-talking-in-sansad-using-jansansad-tech-blog-b6cae4429221  
Social Blog (results) - https://medium.com/@siddharthbanga/what-are-they-talking-in-sansad-using-jansansad-social-blog-40822793ef3f

# How to Set up
## 1. Clone the repository
`git clone https://github.com/your-username/jansansad.git`  
`cd jansansad`
## 2. Start PostgreSQL using Docker Compose
Ensure Docker is installed, then run:
`docker-compose up -d`
## 3. Use Jansansad commands
### jansansad download
To download all the transcripts from 13th to 17th Lok Sabha sessions (1283 PDFs) into respective folders  
### jansansad populate
Extracts all the words, cleans, processes and stores word counts into Postgres tables
### jansansad.bat query
Interactively query word usage across sessions (single/double) - Also get it plotted

# Sample outputs
CLI - 
`Do you want to search for a single word or a double (single/double): single
Enter a word to search: agriculture

Searching for word: 'agriculture' in all tables...

For Session13: 20967  
For Session14: 23642  
For Session15: 17757  
For Session16: 4622  
For Session17: 176`

Sample absolute chart - 
![image width="50" height="50"](https://github.com/user-attachments/assets/436feab1-45b1-4afc-b211-7c9f39858acc)



