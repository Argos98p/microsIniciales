package com.example.springsocial.triplestore;

import com.example.springsocial.model.User;
import com.github.slugify.Slugify;

public class QueryTemplates {

    static final Slugify slg = Slugify.builder().underscoreSeparator(true).build();

    public static String queryInsertUser(User user){

        String role = " \""+user.getRole()+"\"";
        Long id= user.getId();
        String name= " \""+user.getName()+"\"";
        String nick= " \""+slg.slugify(user.getName())+"\";";
        return """
        prefix : <http://turis-ucuenca/>
        prefix foaf: <http://xmlns.com/foaf/0.1/>
        prefix myusers: <http://turis-ucuenca/user/>
        base  <http://turis-ucuenca/>

        INSERT {
            myusers:"""+id+"""
              a  foaf:Person;
            foaf:name""" +name+"""
            ;
            foaf:mbox <"""+ user.getEmail() +"""
            > ;
            foaf:nick"""+nick+"""
            :role"""+ role+"""
            ;
            foaf:depiction  <adfadsf.com/user.png>.
        } WHERE { }
        """;
    }



    public static String updateRoleUser(String newRole, String oldRole, String idUser){

        newRole=" \""+newRole+"\"";
        oldRole=" \""+oldRole+"\"";
        String urlUser=" \""+"http://turis-ucuenca/user/"+idUser+"\"";

        return """
                prefix : <http://turis-ucuenca/>
                prefix foaf: <http://xmlns.com/foaf/0.1/>
                prefix myusers: <http://turis-ucuenca/user/>
                base  <http://turis-ucuenca/>
                                
                DELETE { ?id :role
                 """+ oldRole+ """
                }
                INSERT {
                    ?id  a  foaf:Person;
                   :role
                   """+newRole+"""
                   .
                }
                 WHERE {
                    ?id a foaf:Person ;
                  FILTER(str(?id) =
                  """+urlUser+"""
                  ) .
                 }
                """;
    }

    public static String AddUserInOrganization(String userId, String organizationId){
        userId=" myusers:"+userId+" ";
        organizationId = " myorg:"+organizationId+" ";

        return """
                prefix : <http://turis-ucuenca/>
                prefix org: <http://www.w3.org/TR/vocab-org/>
                prefix myorg: <http://turis-ucuenca/org/>
                prefix myusers: <http://turis-ucuenca/user/>
                prefix foaf: <http://xmlns.com/foaf/0.1/>
                base  <http://turis-ucuenca/>                              
                INSERT {                    
                     """ +userId+ """ 
                        a  foaf:Person ;
                        foaf:memberOf
                         """+organizationId+"""
                         .
                    """+organizationId+"""
                     a org:Organization;
                        org:hasMember
                         """+userId+"""
                         .
                } WHERE{}
                """;
    }


}
