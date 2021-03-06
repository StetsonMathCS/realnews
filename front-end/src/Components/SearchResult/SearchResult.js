import React from 'react';
import { Link } from "react-router-dom";
import './SearchResult.css';

const GridItem = ({title, summary, id}) => {
    return (
        <Link to={`/article/+${id}`}>
            <div className="grid hover:border-blue bg-blue-darker border-r-2 border-b-2 border-l-2 border-t-2 border-blue-darker rounded">
                    <h2 className="font-bold text-base md:text-lg mb-2 text-center text-green-lighter pt-5">{`${title + ''}`}</h2>
                    <div className="text-grey text-base pt-3 mb-1 px-5 showtwolines">{`${summary + ''}`}</div>
            </div>
            <br />
            <br />
        </Link>
    );
}

export default GridItem;