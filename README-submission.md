### Team Number 5
## Team Members

| Last Name    | First Name | GitHub User Name |
|--------------|------------|------------------|
| Rios Negrete | Edgar      | edgar-damian     |
| Dominguez    | Diego      | DiegoDomingu3z   |
| Acuna        | Oscar      | Stein065         |

# Test Results

#### How many of the dumpfiles matched (using the check-dump-files.sh script)? 
9/9 tests passed.

#### How many of the btree query files results matched (using the check-btree-search.sh script)?
18/18
#### How many of the database query files results matched (using the check-db-search.sh script)?
9/9

# AWS Notes
Brief reflection on your experience with running your code on AWS.

# Reflection

Provide a reflection by each of the team member (in a separate subsection)

## Reflection (Team member name: Edgar Rios Negrete)
I gained a lot of experience from this project, especially in terms of working with other on git. This project was fairly big, 
and we had numerous different things all come in and tie together towards the end. Github was great for keeping track of what
each person did. The scrum board was also a very helpful feature, I discovered the benefits of using this on the later half of 
the project, so I did not use it as much as I wish I did. This project really tested my character with the amount of bugs that 
we had to fix. Overall, me fixing so many bugs gave me a confidence boost. One bug in particular took me hours to find and figure 
out. When our SSHFileReader grabs a user name and an IP, it will return a big string with both of these values operated by a “-‘”. 
It all goes as normal, until it is written in the disk. Since there are 64 bytes, that means there can be a max of 32 chars in a
single string per TreeObject. When it is getting written to the disk, it gets truncated at 32 chars. This then in a way changes the key. 
When a duplicate comes along, it does not find a match, as the original is now truncated and they no longer equal. Then it gets 
inserted as a new one and gets truncated as it gets written into the disk. This then causes a duplicate to appear in the final output. 
Finding this was time consuming to say the least, but I feel accomplished now that I was able to clear that up, and that me and my group 
were able to get all of the different parts of the code to work together an work as intended. Looking back at it, I think it is impressive 
how much we did on this project and how many different things we brought in form the semester.

## Reflection (Team member name: Diego Dominguez)
This project was by far the most challenging this semester, setting up the SSH files was quite hard to begin with
just because I fully didn't understand how to parse everything and use those to create our btress and write it to our needed files.
Also figuring out the sqlite database was sort of hassle, just because the table names were somehow not the same
so it took a while to figure out why it kept failing. I have had quite a bit experience with writing sql statements so that part
came a bit easier to me. Overall I think this project was very valuable in terms of putting our learning from our lectures
into practice and as well learning how to work with a team. 

## Reflection (Team member name: )

# Additional Notes
TBD

