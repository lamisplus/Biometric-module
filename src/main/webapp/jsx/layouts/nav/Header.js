import React, {useEffect, useState} from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import { Dropdown } from "react-bootstrap";
import {url as baseUrl} from "./../../../api";
/// Image
import avatar from "../../../images/avatar/1.jpg";
import LogoutPage from './Logout';
import {  toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { authentication } from "./../../../_services/authentication";
import * as ACTION_TYPES from "./../../../actions/types";
import store from "./../../../store";
//import AssignFacilityModal from './../../components/Users/AssignFacilityModalFirst'

const { dispatch } = store;

const Header = (props) => {
  const [user, setUser] = useState(null);
  //const [modal, setModal] = useState(false);

  const [assignFacilityModal, setAssignFacilityModal] = useState(false);
   //TO ASSIGN FACILITIES
   const toggleAssignModal = () => {
    setAssignFacilityModal(!assignFacilityModal);
  }
  async function fetchMe() {
    if( authentication.currentUserValue != null ) {
      axios
          .get(`${baseUrl}account`)
          .then((response) => {
            setUser(response.data);
            // set user permissions in local storage for easy retrieval, when user logs out it will be removed from the local storage
            localStorage.setItem('currentUser_Permission', JSON.stringify(response.data.permissions));
            dispatch({
              type: ACTION_TYPES.FETCH_PERMISSIONS,
              payload: response.data.permissions,
            });

            if(response.data && response.data.currentOrganisationUnitId === null ){
              toggleAssignModal()
            }            
            
          })
          .catch((error) => {
            authentication.logout();
           // console.log(error);
          });
    }
  }

  async function switchFacility (facility) {
    console.log(facility)
    await axios.post(`${baseUrl}users/organisationUnit/${facility.value.organisationUnitId}`, {})
        .then(response => {
          toast.success('Facility switched successfully!');
          fetchMe();
          //toggleAssignFacilityModal();
        }) .catch((error) => {
         toast.error('An error occurred, could not switch facilty.');
        });

  }

  const currentUser = authentication.getCurrentUser();

  useEffect(() => {
    fetchMe();

  }, []);


  return (
    <div className="header" style={{ backgroundColor: '#303f9f', height:'55px' }}>
      <div className="header-content">
        <nav className="navbar navbar-expand">
          <div className="collapse navbar-collapse justify-content-between">
            <div className="header-left">
				<div className="input-group search-area d-xl-inline-flex d-none">
					{/* Content can go here */}
				</div>
            </div>
            <ul className="navbar-nav header-right main-notification">
                          <Dropdown as="li" className="nav-item dropdown header-profile">
                          <Dropdown.Toggle variant="" as="a" className="nav-link i-false c-pointer"
                                role="button" data-toggle="dropdown"
                          >
                            <div className="header-info me-3">
                              
                              <small className="text-end fs-14 font-w400" style={{ color: '#ffffff' }}>{currentUser.name}</small>
                            </div>
                            <img src={avatar} width={20} alt="" />
                                  </Dropdown.Toggle>

                                  <Dropdown.Menu align="right" className="mt-2 dropdown-menu-end">
                                  <Link to="#" className="dropdown-item ai-icon" onClick={()=> window.open("https://datafinigeria.on.spiceworks.com/portal", "_blank")}>
                                    <svg
                                      id="icon-user1" xmlns="http://www.w3.org/2000/svg" className="text-primary"
                                      width={18} height={18} viewBox="0 0 24 24" fill="none"
                                      stroke="currentColor" strokeWidth={2} strokeLinecap="round"strokeLinejoin="round"
                                    >
                                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                                      <circle cx={12} cy={7} r={4} />
                                    </svg>
                                    <span className="ms-2">Help </span>
                                  </Link>
                        <LogoutPage />
                  </Dropdown.Menu>
                </Dropdown>
            </ul>
          </div>
        </nav>
      </div>
      {/* <AssignFacilityModal showModal={assignFacilityModal} toggleModal={() => setAssignFacilityModal(!assignFacilityModal)} user={user}/> */}
    </div>
  );
};

export default Header;
