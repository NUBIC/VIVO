<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<@widget name="login" include="assets" />
                       
<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
    </head>
    
    <body>
        <#include "identity.ftl">
            
        <#include "menu.ftl">
        
            <section id="intro" role="region">
            <h2>Welcome to VIVO</h2>
            
            <p>VIVO is a research-focused discovery tool that enables collaboration among scientists across all disciplines.</p>
            <p>Browse or search information on people, departments, courses, grants, and publications.</p>
            
                <section id="search-home" role="region">
                    <h3>Search VIVO</h3>
                    
                    <fieldset>
                        <legend>Search form</legend>
                        <form id="search-home-vivo" action="${urls.search}" method="post" name="search-home" role="search">
                            <#if user.showFlag1SearchField>
                                <select id="search-form-modifier" name="flag1" class="form-item" >
                                    <option value="nofiltering" selected="selected">entire database (${user.loginName})</option>
                                    <option value="${portalId}">${siteTagline!}</option>
                                </select>
                            <#else>
                                <input type="hidden" name="flag1" value="${portalId}" />
                           </#if> 
                            <div id="search-home-field">
                                <input type="text" name="querytext" class="search-home-vivo" value="${querytext!}" />
                                <input type="submit" value="Search" class="submit">
                            </div>
                        </form>
                    </fieldset>
                </section> <!-- #search-home -->
            </section> <!-- #intro -->
            
            <@widget name="login" />
            
            <#include "browse-classgroups.ftl">
        
        <#include "footer.ftl">
    </body>
</html>