import React, { Component } from 'react';
import ListItem from '../ListItem/ListItem';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import Logo from '../Logo/Logo.js';
import Search from '../Search/Search.js'
import InfiniteScroll from 'react-infinite-scroller'
import GridItem from '../ListItem/GridItem'
import GridItem2 from '../ListItem/GridItem2'
class SearchResults extends Component {
    constructor(props) {
        super(props);
        this.state = {
            search: String(props.match.params.id).split("+").join(" ").replace(/ /, ""),
            articles: [],
            start: 0
        };
    }

    componentDidMount() {
        this.loadItems();
    }
/*
    response (response) {
        response = response.json();
        this.setState({
            start: (this.state.start + 10)
        })
        for(let doc of response.docs) {
            this.state.listItems.push(doc);
        }
    }

    load () {
        let body = process.env.REACT_APP_URL + "/solr/monkey/select?q=summary%3A" + encodeURIComponent(this.state.search) + "&start=" + this.state.start + "&wt=json&json.wrf=response";
        let script = document.createElement('script');
        script.src = body;
        document.body.appendChild(script);
    }
*/
    
    loadItems() {
        let body = process.env.REACT_APP_URL + "/solr/monkey/select?q=summary%3A" + encodeURIComponent(this.state.search) + "&start=" + this.state.start + "&wt=json";
        console.log(body);
        fetch(body, { 
            mode: "cors",
            headers: { "Content-Type": "application/json",
                        "Authorization": "Basic" + window.btoa(process.env.REACT_APP_USERNAME + ":" + process.env.REACT_APP_PASSWORD)}, 
            method: "GET" 
        })
        .then(response => {
            if (response.ok) {
                console.log(response);
            } else {
                this.setState({ hasMore: false });
                throw new Error('failed to fetch articles');
            }
        })
        .then(response => response.json())
        .then(data => {
            if(data.numFound <= 10) this.setState({hasMore: false});
            this.setState({
                articles:  data.docs,
                start: (this.state.start + 10)
            });
        })
    }    

    render() {

        let items = [];
        this.state.articles.map((article, i) => {
            items.push(
                <GridItem title={article.title} summary={article.summary} key={i}/>
            );
        });

        return (
            <div className = "container mx-auto bg-blue-darkest" >
                <center>
                <Logo />
                <Search />
                <br />
                <h1 className = "pt-3 pb-5 text-green-lighter font-bold">Your Search Results!</h1>
                <br />
                <br />
                <div>
                <InfiniteScroll
                    pageStart={0}
                    loadMore={this.loadItems()}
                    hasMore={this.state.hasMore}
                    loader={<div>Loading ...</div>}
                    >
                    {items}
                </InfiniteScroll>
                </div>
                    <br />
                    <br />
                </center>
            </div>
        );
    }
}

export default SearchResults;