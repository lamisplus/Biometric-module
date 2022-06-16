import React, { Fragment, useContext, useState } from "react";
/// React router dom
import { Link } from "react-router-dom";
import { ThemeContext } from "../../../context/ThemeContext";
import logo from "../../../images/lamisPlus/lamislogo.png";

const NavHader = () => {
  const [toggle, setToggle] = useState(false);
  const {openMenuToggle} = useContext(
    ThemeContext
  );
  return (
    <div className="nav-header" style={{ backgroundColor: '#f2f7f8',height:'55px' }}>
      <Link to="/dashboard" className="brand-logo" >
			<Fragment style={{padding:'5px'}}>
                <img src={logo} alt="" className="logo-abbr"  width="50" height="40"/>
                {"  "}
                <text style={{color:'#303f9f'}}>{!toggle?'LamisPlus':''}</text>
			</Fragment>
      </Link>

      <div
        className="nav-control"
        onClick={() => {
          setToggle(!toggle);
          openMenuToggle();
        }}
      >
        <div className={`hamburger ${toggle ? "is-active" : ""}`}>
          <span className="line" ></span>
          <span className="line" ></span>
          <span className="line"></span>
        </div>
      </div>
    </div>
  );
};

export default NavHader;
