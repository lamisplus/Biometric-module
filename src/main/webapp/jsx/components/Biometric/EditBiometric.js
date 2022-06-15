import React, { useState}   from 'react';
import {
 Spinner
} from 'reactstrap';
import MatButton from '@material-ui/core/Button';
import {Modal, Button} from 'react-bootstrap';
import axios from "axios";
import {url as baseUrl} from "../../../api";
//import EditIcon from "@material-ui/icons/EditIcon";
import {makeStyles} from "@material-ui/core/styles";
import {toast} from "react-toastify";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";


const useStyles = makeStyles(theme => ({
    button: {
        margin: theme.spacing(1)
    },
    error: {
        color: "#f85032",
        fontSize: "12.8px",
    },
}))

const AddBiometricDevice = (props) => {
    const classes = useStyles()
    const [loading, setLoading] = useState(false)
    const datasample = props.datasample ? props.datasample : {};
    const [errors, setErrors] = useState({});
    const deviceItems = { type: "", name:"", url:"", }
    const [details, setDetails] = useState(deviceItems);



    const handleOtherFieldInputChange = e => {
        setDetails ({ ...details, [e.target.name]: e.target.value });
    }
    const validate = () => {
        let temp = { ...errors }
        //temp.parentId = details.parentId!=="" ? "" : "This field is required"
        temp.url = details.url ? "" : "This field is required"
        temp.name = details.name ? "" : "This field is required"
        temp.type = details.type ? "" : "This field is required"
        setErrors({
            ...temp
        })
        return Object.values(temp).every(x => x == "")
    }

    //Function to cancel the process
    const closeModal = ()=>{
        //resetForm()
        props.togglestatus()
        //setDetails(defaultDetailValues)
        //setErrors({})
    }

    //Method to update module menu
    const AddDevice = e => {
        e.preventDefault()

        if(validate()){
            details["moduleId"] = datasample.id

            setLoading(true);
            const onSuccess = () => {
                props.loadModuleMenus()
                props.togglestatus()
                setLoading(false)
            }
            const onError = () => {
                setLoading(false)
            }
            props.addMenu(details, onSuccess, onError);
            return

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
                                <div className="basic-form">
                                    <form onSubmit={(e) => e.preventDefault()}>
                                       
                                        <div className="row">
                                            
                                            <div className="form-group col-md-12">
                                                <label> Name</label>
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
                                                <label>Url</label>
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
                                                <label>Device type</label>
                                                <input
                                                    type="text"
                                                    name="type"
                                                    id="type"
                                                    className="form-control"
                                                    value={details.type}
                                                    onChange={handleOtherFieldInputChange}
                                                />
                                                {errors.type !=="" ? (
                                                    <span className={classes.error}>{errors.type}</span>
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

                                            <span style={{textTransform: 'capitalize'}}>Update  {loading ? <Spinner /> : ""}</span>
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


export default AddBiometricDevice;
