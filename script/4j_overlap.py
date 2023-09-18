import re
import os
import pandas as pd
import time
path1 = "/framework/projects/"
gbr_path = "/root/"+"GrowingBugRepository"+path1
d4j_path = "/root/"+"defects4j"+path1
gbr_bugs = "/root/Repair/test.csv"


def write_to_our_csv(our,their,equal):
    our['defects4jBugFlag'] = equal
    our['buggyVersion'] = their['revision.id.buggy']
    our['fixedVersion'] = their['revision.id.fixed']
    our['reportId'] = their['report.id']
    our['reportUrl'] = their['report.url']
bugs_info = pd.read_csv(gbr_bugs)
columns = ['id','projectId','projectName','subProjectLocator','bugId','buggyVersion','fixedVersion','reportId','reportUrl','defects4jBugFlag']
lack = ""
lack_set = set()
for index, row in bugs_info.iterrows():
    bugs_info.loc[index,['projectId']]  = row['projectId'].replace('<br/>','').replace('</br>','')

    row = bugs_info.loc[index]    
    p = row['projectId']
    id = row['bugId']
    d4j_active_path = d4j_path+p+'/active-bugs.csv'
    gbr_active_path = gbr_path+p+"/active-bugs.csv"
    #if not (p == "Leshan_core" and id == 8):
    #    continue
    #print(id)
    if not os.path.exists(gbr_active_path):
        if p not in lack_set:
            lack_set.add(p)
            lack += p+"\n"
        continue
    gbr_active_bugs = pd.read_csv(gbr_active_path)

    #d4j doen't have that project

    if not os.path.exists(d4j_active_path):
        found = False
        for i,gbr_row in gbr_active_bugs.iterrows():
            if gbr_row['bug.id'] == id:
                found = True
                bugs_info.loc[index,['defects4jBugFlag']] = 0
                bugs_info.loc[index,['buggyVersion']] = gbr_row['revision.id.buggy']
                bugs_info.loc[index,['fixedVersion']] = gbr_row['revision.id.fixed']
                bugs_info.loc[index,['reportId']] = gbr_row['report.id']
                bugs_info.loc[index,['reportUrl']] = gbr_row['report.url']
                #write_to_our_csv(row,gbr_row,False)
                break
        if not found:
            lack += p+"-"+str(id)+"\n"
        continue
    d4j_active_bugs = pd.read_csv(d4j_active_path)

    #d4j doesn't has that bug
    d4j_ids = d4j_active_bugs['bug.id']
    if id not in list(d4j_ids):
        for i,gbr_row in gbr_active_bugs.iterrows():
            if gbr_row['bug.id'] == id:
                bugs_info.loc[index,['defects4jBugFlag']] = 0
                bugs_info.loc[index,['buggyVersion']] = gbr_row['revision.id.buggy']
                bugs_info.loc[index,['fixedVersion']] = gbr_row['revision.id.fixed']
                bugs_info.loc[index,['reportId']] = gbr_row['report.id']
                bugs_info.loc[index,['reportUrl']] = gbr_row['report.url']
                #write_to_our_csv(row,gbr_row,False)
                break
        continue

    equal = False
    matched = False
    for i,gbr_row in gbr_active_bugs.iterrows():
        if gbr_row['bug.id'] == id:
            for j,d4j_row in d4j_active_bugs.iterrows():
                if d4j_row['bug.id'] == id:
                    matched = True
                    if gbr_row['revision.id.buggy'] == d4j_row['revision.id.buggy'] and gbr_row['revision.id.fixed'] == d4j_row['revision.id.fixed']:
                        equal = True
        #found, and equal
        if equal:
            bugs_info.loc[index,['defects4jBugFlag']] = 1
            bugs_info.loc[index,['buggyVersion']] = gbr_row['revision.id.buggy']
            bugs_info.loc[index,['fixedVersion']] = gbr_row['revision.id.fixed']
            bugs_info.loc[index,['reportId']] = gbr_row['report.id']
            bugs_info.loc[index,['reportUrl']] = gbr_row['report.url']
            break
        #found, but not equal
        elif matched:
            bugs_info.loc[index,['defects4jBugFlag']] = 0
            bugs_info.loc[index,['buggyVersion']] = gbr_row['revision.id.buggy']
            bugs_info.loc[index,['fixedVersion']] = gbr_row['revision.id.fixed']
            bugs_info.loc[index,['reportId']] = gbr_row['report.id']
            bugs_info.loc[index,['reportUrl']] = gbr_row['report.url']
            break
            #else: not found ,continue loop
timestamp = int(time.time())
bugs_info.to_csv('./data/all_bugs.csv',mode='w')
open('lack.txt','w').write(lack)