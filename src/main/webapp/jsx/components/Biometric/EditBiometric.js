import React, { useState, useEffect}   from 'react';
import {
 Spinner
} from 'reactstrap';
import MatButton from '@material-ui/core/Button';
import {Modal, Button} from 'react-bootstrap';
import axios from "axios";
//import EditIcon from "@material-ui/icons/EditIcon";
import {makeStyles} from "@material-ui/core/styles";
import {toast} from "react-toastify";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import { token as token, url as baseUrl } from "./../../../api";

const useStyles = makeStyles((theme) => ({
    card: {
        margin: theme.spacing(20),
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    form: {
        width: "100%", // Fix IE 11 issue.
        marginTop: theme.spacing(3),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    cardBottom: {
        marginBottom: 20,
    },
    Select: {
        height: 45,
        width: 300,
    },
    button: {
        margin: theme.spacing(1),
    },
    root: {
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));

const EditBiometricDevice = (props) => {
    const classes = useStyles()
    const [loading, setLoading] = useState(false)
    const datasample = props.datasample ? props.datasample : {};
    const [errors, setErrors] = useState({});
    const [details, setDetails] =  useState(props.datasample )
    useEffect(() => {
        setDetails(props.datasample)
    }, [props.datasample]);

    const handleOtherFieldInputChange = e => {
        setDetails ({ ...details, [e.target.name]: e.target.value });
    }
    const validate = () => {
        let temp = { ...errors }
        //temp.parentId = details.parentId!=="" ? "" : "This field is required"
        temp.url = details.url ? "" : "This field is required"
        temp.name = details.name ? "" : "This field is required"
        temp.active = details.active ? "" : "This field is required"
        //temp.port = details.port ? "" : "This field is required"
        setErrors({
            ...temp
        })
        return Object.values(temp).every(x => x == "")
    }

    //Function to cancel the process
    const closeModal = ()=>{
        //resetForm()
        props.togglestatus()
    }

    //Method to update module menu
    const AddDevice = e => {
        e.preventDefault()
        if(validate()){
            axios
            .put(`${baseUrl}biometrics/device/${details.id}?active=${details.active}`,details,
            { headers: {"Authorization" : `Bearer ${token}`} }
            )
            .then((response) => {                
                props.loadBiometricDevices()               
                toast.success("Biometric Device Updated Successfully!")
                setDetails({active: "", name:"", url:"", port:"", type:""})
                props.togglestatus()                  
            })
            .catch((error) => { 
                toast.error("Something went wrong. Please try again...")   
            });
        }  
    }

    return (
        <div >

            <Modal show={props.modalstatus} toggle={props.togglestatus} className={props.className} size="md">
                <Modal.Header toggle={props.togglestatus}>
                    <Modal.Title>Edit Biometric Device</Modal.Title>
                    <Button
                        variant=""
                        className="btn-close"
                        onClick={props.togglestatus}
                    ></Button>
                </Modal.Header>
                <Modal.Body>
     
                    <div className="col-md-12 col-md-12">
                        <div className="card">

                            <div className="card-body">
                                <div className={classes.root}>
                                    <form onSubmit={(e) => e.preventDefault()}>
                                       
                                        <div className="row">
                                            
                                            <div className="form-group col-md-12">
                                                <label> Name *</label>
                                                <input
                                                    type="text"
                                                    name="name"
                                                    id="name"
                                                    className="form-control"
                                                    value={details.name}
                                                    onChange={handleOtherFieldInputChange}
                                                />
                                                {errors.name !=="" ? (
                                                    <span className={classes.error}>{errors.name}</span>
                                                ) : "" }
                                            </div>
                                            <div className="form-group col-md-12">
                                                <label>Url *</label>
                                                <input
                                                    type="text"
                                                    name="url"
                                                    id="url"
                                                    className="form-control"
                                                    value={details.url}
                                                    onChange={handleOtherFieldInputChange}
                                                    required
                                                />
                                                
                                                 {errors.url !=="" ? (
                                                    <span className={classes.error}>{errors.url}</span>
                                                ) : "" }
                                            </div>
                                            <div className="form-group col-md-12">
                                                <label>Port </label>
                                                <input
                                                    type="text"
                                                    name="port"
                                                    id="port"
                                                    className="form-control"
                                                    value={details.port}
                                                    onChange={handleOtherFieldInputChange}
                                                    required
                                                />

                                            </div>
                                            <div className="form-group col-md-12">
                                                <label>Default *</label>
                                                
                                                <select

                                                name="active"
                                                id="active"
                                                className="form-control wide"
                                                value={details.active}
                                                onChange={handleOtherFieldInputChange}
                                                >  
                                                <option value=""> Select</option>                                 
                                                <option value="true"> Yes</option>
                                                <option value="false">No</option>
                                                </select>
                                                {errors.active !=="" ? (
                                                    <span className={classes.error}>{errors.active}</span>
                                                ) : "" } 
                                            </div>

                                            {/*Second Row of the Field by Col */}

                                        </div>
                                        <MatButton
                                            type='submit'
                                            variant='contained'
                                            color='primary'
                                            className={classes.button}
                                            startIcon={<SaveIcon />}
                                            disabled={loading}
                                            onClick={AddDevice}

                                        >

                                            <span style={{textTransform: 'capitalize'}}>Save  {loading ? <Spinner /> : ""}</span>
                                        </MatButton>
                                        <MatButton
                                            variant='contained'
                                            color='default'
                                            onClick={closeModal}
                                            startIcon={<CancelIcon />}>
                                            <span style={{textTransform: 'capitalize'}}>Cancel</span>
                                        </MatButton>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                        
                </Modal.Body>
            </Modal>
        </div>
    );
}


export default EditBiometricDevice;
